/*
 * Copyright 2017, Regents of the Univ(ersity of Lancaster
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 * 
 *  * Neither the name of the University of Lancaster nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *
 * Author: Steven Simpson <s.simpson@lancaster.ac.uk>
 */
package uk.ac.lancs.routing.hier;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Describes a required connection in terms of terminal end points and
 * minimum bandwidth allocation.
 * 
 * @author simpsons
 */
public final class ConnectionRequest {
    /**
     * The set of terminal end points in the connection
     */
    public final Collection<? extends EndPoint> terminals;

    /**
     * The minimum bandwidth of the connection
     */
    public final long bandwidth;

    private ConnectionRequest(Collection<? extends EndPoint> termini,
                              long bandwidth) {
        this.terminals =
            Collections.unmodifiableCollection(new HashSet<>(termini));
        this.bandwidth = bandwidth;
    }

    /**
     * Create a connection request.
     * 
     * @param termini the termini of the required connection
     * 
     * @param bandwidth the amount of bandwidth to be allocated
     * 
     * @return a description of the requested connection
     */
    public static ConnectionRequest of(Collection<? extends EndPoint> termini,
                                       long bandwidth) {
        return new ConnectionRequest(termini, bandwidth);
    }
}
