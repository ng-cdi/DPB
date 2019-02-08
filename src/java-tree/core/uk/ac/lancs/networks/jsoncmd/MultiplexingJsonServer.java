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

package uk.ac.lancs.networks.jsoncmd;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads JSON messages from a base channel, and creates new sesson
 * channels according to the
 * <samp>{@value MultiplexingJsonChannelManager#DISCRIMINATOR}</samp>
 * field.
 * 
 * @author simpsons
 */
public class MultiplexingJsonServer extends MultiplexingJsonChannelManager
    implements JsonChannelManager {

    /**
     * Create a server to respond to session-specific messages on a base
     * channel.
     * 
     * @param base the base channel on which to multiplex sessions
     */
    public MultiplexingJsonServer(JsonChannel base) {
        super(base);
    }

    private final List<SessionChannel> queue = new ArrayList<>();

    /**
     * Get the next session channel on the base channel. Unless another
     * thread is performing the same action, the calling thread will
     * continuously read messages and queue them to the appropriate
     * session channel, until an unrecognized session is encountered,
     * causing a new session channel to be created and returned.
     * 
     * @return the next session channel, or {@code null} if there are no
     * more because the base channel has been closed
     */
    @Override
    public synchronized JsonChannel getChannel() {
        while (queue.isEmpty() && !terminated) {
            if (inUse) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // Do nothing.
                }
                continue;
            }
            process(null);
        }
        if (!queue.isEmpty()) return queue.remove(0);
        return null;
    }

    @Override
    SessionChannel open(int id) {
        /* As a server, we create sessions when the client tells us
         * to. */
        SessionChannel result = new SessionChannel(id);

        /* Store the session under its id, so that messages received
         * with this id can be queued with this session channel. */
        sessions.put(id, result);

        /* Make the channel available in the queue, so that getChannel
         * can read from it. */
        queue.add(result);
        notifyAll();
        return result;
    }

    @Override
    boolean shouldCloseOnEmpty() {
        return false;
    }
}
