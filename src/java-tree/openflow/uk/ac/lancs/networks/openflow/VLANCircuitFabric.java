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
package uk.ac.lancs.networks.openflow;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Map;

import uk.ac.lancs.networks.TrafficFlow;
import uk.ac.lancs.networks.circuits.Circuit;
import uk.ac.lancs.networks.fabric.Bridge;
import uk.ac.lancs.networks.fabric.BridgeListener;
import uk.ac.lancs.networks.fabric.Fabric;
import uk.ac.lancs.networks.fabric.Interface;

/**
 * Manages the slicing of an OpenFlow switch by VLAN circuit ids through
 * a REST interface on its controller.
 * 
 * @author simpsons
 */
public final class VLANCircuitFabric implements Fabric {
    private final long dpid;
    private final VLANCircuitControllerREST sliceRest;

    /**
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * 
     */
    public VLANCircuitFabric(int portCount, long dpid, URI ctrlService,
                             X509Certificate ctrlCert, String ctrlAuthz)
        throws KeyManagementException,
            NoSuchAlgorithmException {
        this.dpid = dpid;
        this.sliceRest =
            new VLANCircuitControllerREST(ctrlService, ctrlCert, ctrlAuthz);
        throw new UnsupportedOperationException("unimplemented"); // TODO
    }

    @Override
    public Interface<?> getInterface(String desc) {
        throw new UnsupportedOperationException("unimplemented"); // TODO
    }

    @Override
    public Bridge
        bridge(BridgeListener listener,
               Map<? extends Circuit<? extends Interface<?>>, ? extends TrafficFlow> details) {
        throw new UnsupportedOperationException("unimplemented"); // TODO
    }

    @Override
    public void retainBridges(Collection<? extends Bridge> bridges) {
        throw new UnsupportedOperationException("unimplemented"); // TODO
    }

    @Override
    public int capacity() {
        return 1;
    }
}
