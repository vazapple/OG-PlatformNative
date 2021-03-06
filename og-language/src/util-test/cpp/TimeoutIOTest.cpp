/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#ifndef _WIN32
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#endif /* ifndef _WIN32 */

#include <util/cpp/TimeoutIO.h>

LOGGING (com.opengamma.language.util.TimeoutIOTest);

#define TIMEOUT_WAIT	2000

// Note: Most of CTimeoutIO is tested by NamedPipeTest and BufferedInputTest

#ifndef _WIN32
static int _createSocket () {
	int sock = socket (AF_INET, SOCK_STREAM, 0);
	struct sockaddr_in addr;
	memset (&addr, 0, sizeof (addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = htonl (INADDR_ANY);
	addr.sin_port = 0;
	bind (sock, (struct sockaddr*)&addr, sizeof (addr));
	listen (sock, 5);
	return sock;
}
#endif /* ifndef _WIN32 */

class CLazyCloseTest : public CTimeoutIO {
public:

	unsigned long m_lTimeout;
	bool m_bSignalled;

	CLazyCloseTest (unsigned long lTimeout) : CTimeoutIO (
#ifdef _WIN32
			NULL
#else /* ifdef _WIN32 */
			_createSocket ()
#endif /* ifdef _WIN32 */
	) {
		m_lTimeout = lTimeout;
		m_lTimeout = lTimeout;
	}

	// This code has to match the logic in WaitOnOverlapped and BeginOverlapped/EndOverlapped
	void Run () {
#ifdef _WIN32
		if (WaitForSingleObject (GetOverlapped ()->hEvent, m_lTimeout) == WAIT_OBJECT_0) {
			LOGDEBUG (TEXT ("Event signalled"));
			m_bSignalled = true;
		} else {
			LOGDEBUG (TEXT ("Event timedout"));
			m_bSignalled = false;
		}
#else /* ifdef _WIN32 */
		if (BeginOverlapped (m_lTimeout, true)) {
			ASSERT (FALSE);
		} else {
			int nError = GetLastError ();
			if (nError == ETIMEDOUT) {
				LOGDEBUG (TEXT ("Event timedout"));
				m_bSignalled = false;
			} else if (nError == EINTR) {
				LOGDEBUG (TEXT ("Event signalled"));
				m_bSignalled = true;
			} else {
				ASSERT (FALSE);
			}
		}
#endif /* ifdef _WIN32 */
	}

};

static void TimeoutNoLazyClose () {
	CLazyCloseTest oTest (TIMEOUT_WAIT / 2);
	oTest.Run ();
	ASSERT (!oTest.m_bSignalled);
}

static void LazyCloseBeforeIO () {
	CLazyCloseTest oTest (TIMEOUT_WAIT * 3);
	oTest.LazyClose (TIMEOUT_WAIT / 2);
	oTest.Run ();
#ifdef _WIN32
	ASSERT (oTest.m_bSignalled);
#else /* ifdef _WIN32 */
	ASSERT (!oTest.m_bSignalled);
#endif /* ifdef _WIN32 */
}

class CLazyCloseThread : public CThread {
private:
	CTimeoutIO *m_poTimeoutIO;
public:
	CLazyCloseThread (CTimeoutIO *poTimeoutIO) {
		m_poTimeoutIO = poTimeoutIO;
		ASSERT (Start ());
	}
	void Run () {
		LOGDEBUG (TEXT ("Sleeping"));
		CThread::Sleep (TIMEOUT_WAIT);
		m_poTimeoutIO->LazyClose (TIMEOUT_WAIT / 2);
	}
};

static void LazyCloseDuringIO () {
	CLazyCloseTest oTest (TIMEOUT_WAIT * 3);
	CLazyCloseThread *poCloser = new CLazyCloseThread (&oTest);
	long lStart = GetTickCount ();
	oTest.Run ();
	long lElapse = GetTickCount () - lStart;
	ASSERT (poCloser->Wait ());
	CThread::Release (poCloser);
	ASSERT (oTest.m_bSignalled);
	LOGINFO (TEXT ("Elapsed time ") << lElapse << TEXT ("ms"));
	ASSERT (lElapse < TIMEOUT_WAIT * 2);
}

// Tests the functions and objects in Util/TimeoutIO.cpp
BEGIN_TESTS (TimeoutIOTest)
	UNIT_TEST (TimeoutNoLazyClose)
	UNIT_TEST (LazyCloseBeforeIO)
	UNIT_TEST (LazyCloseDuringIO)
END_TESTS
