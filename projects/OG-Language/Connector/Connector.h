/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_connector_h
#define __inc_og_language_connector_connector_h

#include <assert.h>
#include <Util/Asynchronous.h>
#include <Util/Fudge.h>
#include "Client.h"
#include "SynchronousCalls.h"
#ifdef _WIN32
#include <Util/Library.h>
#endif /* ifdef _WIN32 */

/// Main interface to the library. Most of the functionality is obtained through working with
/// an instanceof CConnector.
///
/// Note the "const" behaviour applies to the "start"/"stop" state, and not the internals needed
/// for message I/O. Thus a "const" connector can be used to send/receive messages but can't be
/// configured.
///
/// This is a reference counted object using the Retain and Release methods.
class CConnector : public CClientService::CStateChange, public CClientService::CMessageReceived {
public:

	/// Callback handler for asynchronous messages posted from the Java stack. These are messages
	/// generated by arbitrary Java execution (e.g. the Live Data components) and not in direct
	/// synchronous response to a message sent from the C++ binding.
	///
	/// This is a reference counted object using the Retain and Release methods.
	class CCallback {
	private:
		friend class CConnector;

		/// Reference count.
		mutable CAtomicInt m_oRefCount;

#ifdef _WIN32
		/// Module lock to prevent the library containing the callback code from being unloaded
		/// until the callback object is released.
		CLibraryLock *m_poModuleLock;
#endif /* ifdef _WIN32 */

	protected:

#ifdef _WIN32
		/// Lock the library containing the callback code. This should be called from the
		/// subclass constructor to ensure that the code remains in the address space.
		///
		/// @param[in] pAddressInModule address in the module that must be locked
		void LockModule (const void *pAddressInModule) {
			assert (!m_poModuleLock);
			m_poModuleLock = CLibraryLock::CreateFromAddress (pAddressInModule);
		}
#endif /* ifdef _WIN32 */

		/// Callback when a message is received.
		///
		/// @param[in] msgPayload the message received
		virtual void OnMessage (FudgeMsg msgPayload) = 0;

		/// Callback when the thread that was calling into OnMessage is about to terminate.
		/// This is to allow the clean-up of TLS or other per-thread operations.
		virtual void OnThreadDisconnect () { }

		/// Destroys the callback object.
		virtual ~CCallback () {
			assert (!m_oRefCount.Get ());
#ifdef _WIN32
			CLibraryLock::UnlockAndDelete (m_poModuleLock);
#endif /* ifdef _WIN32 */
		}

	public:

		/// Creates a new callback object.
		CCallback () : m_oRefCount (1) {
#ifdef _WIN32
			m_poModuleLock = NULL;
#endif /* ifdef _WIN32 */
		}

		/// Increments the reference count.
		void Retain () const { m_oRefCount.IncrementAndGet (); }

		/// Decrements the reference count on the object, deleting it when it reaches zero.
		///
		/// @param[in] poCallback object to release, never NULL
		static void Release (const CCallback *poCallback) { if (!poCallback->m_oRefCount.DecrementAndGet ()) delete poCallback; }

	};

private:

	/// Reference count.
	mutable CAtomicInt m_oRefCount;

	/// Underlying client service that provides the core messaging to/from the Java stack.
	CClientService *m_poClient;

	/// Critical section to protect object state.
	mutable CMutex m_oMutex;

	/// Startup semaphore pointer - during startup a semaphore is posted here to receive notification.
	mutable CAtomicPointer<CSemaphore*> m_oStartupSemaphorePtr;

	/// Asynchronous message callback entry. Each entry includes a class filter allowing it to selectively
	/// receive messages from the Java stack. This allows multiple components to use the single messaging
	/// channel that the underlying communication service provides; for example Live Data, Functions, and
	/// Procedures might be implemented within separate stacks for the language binding.
	///
	/// This is a reference counted object using the Retain and Release methods.
	class CCallbackEntry {
	private:

		/// Reference count.
		mutable CAtomicInt m_oRefCount;

		/// Class to filter - only messages that contain a field with ordinal 0 and this value will
		/// be processed.
		FudgeString m_strClass;

		/// User callback.
		CCallback *m_poCallback;

	public:

		/// Next entry in the singly-linked list of callbacks.
		CCallbackEntry *m_poNext;

		/// Creates a new callback entry.
		///
		/// @param[in] strClass class name to match on, never NULL
		/// @param[in] poCallback callback object to register, never NULL
		/// @param[in] poNext next node in the linked list, or NULL if this is a tail node
		CCallbackEntry (FudgeString strClass, CCallback *poCallback, CCallbackEntry *poNext)
			: m_oRefCount (1) {
			m_strClass = strClass;
			poCallback->Retain ();
			m_poCallback = poCallback;
			m_poNext = poNext;
		}

		/// Destroys the callback entry, releasing resources and the registered callback object.
		~CCallbackEntry () {
			assert (m_oRefCount.Get () == 0);
			if (m_strClass) FudgeString_release (m_strClass);
			CCallback::Release (m_poCallback);
		}

		/// Increments the reference count.
		void Retain () const { m_oRefCount.IncrementAndGet (); }

		/// Decrements the reference count, deleting the object when it reaches zero.
		///
		/// @param[in] poEntry object to release, never NULL
		static void Release (const CCallbackEntry *poEntry) { if (!poEntry->m_oRefCount.DecrementAndGet ()) delete poEntry; }

		/// Tests if the given class name matches the one associated with this callback entry.
		///
		/// @param[in] strClass string to test
		/// @return TRUE if this entry matches, FALSE otherwise
		bool IsClass (FudgeString strClass) const { return !FudgeString_compare (m_strClass, strClass); }

		/// Tests if the given callback object matches the one associated with this callback entry.
		///
		/// @param[in] poCallback object to test
		/// @return TRUE if this entry matches, FALSE otherwise
		bool IsCallback (const CCallback *poCallback) const { return m_poCallback == poCallback; }

		/// Releases the string associated with this entry.
		void FreeString () { FudgeString_release (m_strClass); m_strClass = NULL; }

		/// Called just before a thread that has been making OnMessage calls to this object terminates.
		/// Delegates to the OnThreadDisconnect method of the registered user callback.
		void OnThreadDisconnect () { m_poCallback->OnThreadDisconnect (); }

		void OnMessage (FudgeMsg msgPayload);
	};

	/// Head of the callback entry linked list, or NULL if there are no current callbacks.
	CCallbackEntry *m_poCallbacks;

	/// Synchronous call helper - allocates message identifiers and tracks responses.
	mutable CSynchronousCalls m_oSynchronousCalls;

	/// Asynchronous dispatcher for user callbacks. Within the event thread of CClientService that calls
	/// into this object, the user callback is identified only. The call into the user's callback object
	/// takes place on another thread to allow a continued flow of messages while it is being processed.
	CAsynchronous *m_poDispatch;

	/// Handler for when the client enters its RUNNING state.
	CAtomicPointer<IRunnable*> m_oOnEnterRunningState;

	/// Handler for when the client leaves its RUNNING state (e.g. a restart under error, or a complete
	/// halt as part of a shutdown or more serious error condition).
	CAtomicPointer<IRunnable*> m_oOnExitRunningState;

	/// Handler for when the client enters a state that isn't RUNNING but is not a transient state,
	/// for example STOPPED or ERRORED.
	CAtomicPointer<IRunnable*> m_oOnEnterStableNonRunningState;

	CConnector (CClientService *poClient);
	~CConnector ();
	void OnEnterRunningState ();
	void OnExitRunningState ();
	void OnEnterStableNonRunningState ();
	friend class CConnectorMessageDispatch;
	friend class CConnectorThreadDisconnectDispatch;
	friend class CConnectorDispatcher;
protected:
	void OnStateChange (ClientServiceState ePreviousState, ClientServiceState eNewState);
	void OnMessageReceived (FudgeMsg msg);
	void OnDispatchThreadDisconnect ();
public:

	/// Synchronous message call wrapper. When the outgoing message has been sent, a CCall is constructed.
	/// This can be used to block the caller until the call completes, cancel the call and release the 
	/// resources (it is not possible to cancel execution within the Java stack - the result if it ever
	/// arrives will just be ignored), or poll for completion with timeouts.
	class CCall {
	private:
		friend class CConnector;

		/// The slot allocated by the synchronous call helper to manage the response.
		CSynchronousCallSlot *m_poSlot;

		CCall (CSynchronousCallSlot *poSlot);
	public:
		~CCall ();
		bool Cancel ();
		bool WaitForResult (FudgeMsg *pmsgResponse, unsigned long lTimeout);
	};

	/// Increments the reference count.
	void Retain () const { m_oRefCount.IncrementAndGet (); }

	/// Decrements the reference count, deleting the object when it reaches zero.
	///
	/// @param[in] poConnector object to release, never NULL
	static void Release (const CConnector *poConnector) { if (!poConnector->m_oRefCount.DecrementAndGet ()) delete poConnector; }

	static CConnector *Start (const TCHAR *pszLanguageID);
	bool Stop ();
	bool WaitForStartup (unsigned long lTimeout) const;
	bool Call (FudgeMsg msgPayload, FudgeMsg *pmsgResponse, unsigned long lTimeout) const;
	CCall *Call (FudgeMsg msgPayload) const;
	bool Send (FudgeMsg msgPayload) const;
	bool AddCallback (const TCHAR *pszClass, CCallback *poCallback);
	bool RemoveCallback (const CCallback *poCallback);
	bool RecycleDispatchThread ();
	void OnEnterRunningState (IRunnable *poRunnable);
	void OnExitRunningState (IRunnable *poRunnable);
	void OnEnterStableNonRunningState (IRunnable *poRunnable);
};

#endif /* ifndef __inc_og_language_connector_connector_h */
