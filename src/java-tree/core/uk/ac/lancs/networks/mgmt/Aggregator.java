/*
 * Copyright 2017, Regents of the University of Lancaster
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
package uk.ac.lancs.networks.mgmt;

import uk.ac.lancs.networks.NetworkControl;
import uk.ac.lancs.networks.Terminal;

/**
 * An aggregator consists of a set of inferior networks plus a set of
 * trunks connecting their terminals together.
 * 
 * <p>
 * An aggregator distinguishes between internal and external terminals.
 * External terminals are its own, and can be obtained from the
 * aggregator's {@link NetworkControl#getTerminal(String)} method
 * through {@link Network#getControl()}. Internal terminals belong to
 * inferior networks, and are used to define trunks, or can be
 * associated with external terminals. When creating (external)
 * terminals on an aggregator, they must be mapped to internal
 * terminals.
 * 
 * <p>
 * A trunk connects the terminals of two different inferior networks
 * together by calling
 * {@link #addTrunk(String, String, String, String)}. To implement a
 * service, the aggregator uses its knowledge of trunk topology to plot
 * spanning trees over its trunks, and delegates service requests to the
 * inferior networks owning the terminals at the ends of the trunks that
 * contribute to the spanning tree.
 * 
 * @resume A network that is an aggregate of inferior networks
 * 
 * @author simpsons
 */
public interface Aggregator extends Network {
    /**
     * Create a trunk between two internal terminals within the network.
     * 
     * @param n1 the name of the network owning the first terminal
     * 
     * @param t1 the local name of the first terminal
     * 
     * @param n2 the name of the network owning the second terminal
     * 
     * @param t2 the local name of the second terminal
     * 
     * @return the newly created trunk
     * 
     * @throws NullPointerException if either terminal is null
     * 
     * @throws OwnTerminalException if either of the terminals belongs
     * to this aggregator
     * 
     * @throws NetworkManagementException if a trunk could not be
     * created between the two terminals for other reasons
     * 
     * @constructor
     */
    Trunk addTrunk(String n1, String t1, String n2, String t2)
        throws NetworkManagementException;

    /**
     * Remove and delete a trunk between two internal terminals with the
     * network.
     * 
     * @param network the name of the network owning the specified
     * terminal
     * 
     * @param terminal the local name of either of the trunk's terminals
     * 
     * @param UnknownTrunkException if the terminal did not identify a
     * trunk managed by this aggregator
     * 
     * @throws NetworkManagementException if the terminal could not be
     * removed
     */
    void removeTrunk(String network, String terminal)
        throws NetworkManagementException;

    /**
     * Find an existing trunk connected to a terminal.
     * 
     * @param network the name of the network owning the specified
     * terminal
     * 
     * @param terminal the local name of either of the trunk's terminals
     * 
     * @return the requested trunk, with the terminal as its start, or
     * {@code null} if none exist with that terminal
     */
    Trunk findTrunk(String network, String terminal);

    /**
     * Get an existing trunk connected to a terminal.
     * 
     * @param network the name of the network owning the specified
     * terminal
     * 
     * @param terminal the local name of either of the trunk's terminals
     * 
     * @return the requested trunk
     * 
     * @param UnknownTrunkException if the terminal did not identify a
     * trunk managed by this aggregator
     * 
     * @throws NetworkManagementException if there was an error in
     * getting the trunk
     * 
     * @default This implementation invokes
     * {@link #findTrunk(String, String)}, and returns the result.
     * However, if the result is {@code null}, a
     * {@link TerminalManagementException} is thrown.
     */
    default Trunk getTrunk(String network, String terminal)
        throws NetworkManagementException {
        Trunk result = findTrunk(network, terminal);
        if (result == null)
            throw new UnknownTrunkException(this, network, terminal);
        return result;
    }

    /**
     * Add a new external terminal exposing an inferior network's
     * terminal.
     * 
     * @param name the local name of the terminal
     * 
     * @param subnet the name of the inferior network
     * 
     * @param subterm the local name of the terminal in the inferior
     * network
     * 
     * @return the newly created terminal
     * 
     * @throws TerminalExistsException if the proposed name is already
     * in use as a terminal identifier
     * 
     * @throws NetworkManagementException if the terminal could not be
     * added for other reasons
     */
    Terminal addTerminal(String name, String subnet, String subterm)
        throws NetworkManagementException;
}
