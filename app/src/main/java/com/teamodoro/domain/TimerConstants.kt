package com.teamodoro.domain

/**
 * Cycle geometry, matching the reference Teamodoro web client
 * (https://github.com/BaseSecrete/teamodoro).
 *
 * The reference expresses the cycle as a predicate over wall-clock minutes:
 *
 *     inBreak = (minutes >= 25 && minutes <= 29) || (minutes >= 55 && minutes <= 59)
 *
 * which is a 30-minute cycle of 25 minutes focus followed by 5 minutes break,
 * repeating twice an hour. Because 30 divides 60 evenly, the phase boundaries
 * always land on :00, :25, :30 and :55 of every hour — so any two clients with
 * a correct clock agree on the phase with no server, no room and no offset.
 * That implicit synchronisation is the whole point of the algorithm.
 */
const val CYCLE_MILLIS = 30 * 60 * 1000L
const val WORK_DURATION_MILLIS = 25 * 60 * 1000L
const val BREAK_DURATION_MILLIS = 5 * 60 * 1000L
