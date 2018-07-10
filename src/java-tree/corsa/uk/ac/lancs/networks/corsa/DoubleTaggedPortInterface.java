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
package uk.ac.lancs.networks.corsa;

import uk.ac.lancs.networks.circuits.Circuit;
import uk.ac.lancs.networks.corsa.rest.TunnelDesc;
import uk.ac.lancs.networks.fabric.Interface;
import uk.ac.lancs.networks.fabric.TagKind;

/**
 * 
 * 
 * @author simpsons
 */
final class DoubleTaggedPortInterface implements CorsaInterface {
    final AllPortsInterface base;
    final int port;

    public DoubleTaggedPortInterface(AllPortsInterface base, int port) {
        this.base = base;
        this.port = port;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((base == null) ? 0 : base.hashCode());
        result = prime * result + port;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        DoubleTaggedPortInterface other = (DoubleTaggedPortInterface) obj;
        if (base == null) {
            if (other.base != null) return false;
        } else if (!base.equals(other.base)) return false;
        if (port != other.port) return false;
        return true;
    }

    @Override
    public String toString() {
        return base.toString() + '.' + port + "x2";
    }

    @Override
    public CorsaInterface untag() {
        return base;
    }

    @Override
    public TagKind getTagKind() {
        return TagKind.ENUMERATION;
    }

    @Override
    public int getLabel() {
        return port;
    }

    @Override
    public TunnelDesc configureTunnel(TunnelDesc desc, int label) {
        if (label < 0 || label > getMaximumCircuitLabel())
            throw new IndexOutOfBoundsException("not valid"
                + " double-tagged VLAN: " + label);
        return base.configureTunnel(desc, port).vlanId(label >> 12)
            .innerVlanId(label & 0xfff);
    }

    @Override
    public TagKind getCircuitEncapsulation() {
        return TagKind.VLAN_STAG_CTAG;
    }

    @Override
    public int getMinimumCircuitLabel() {
        return 0;
    }

    @Override
    public int getMaximumCircuitLabel() {
        return 4096 * 4096 - 1;
    }

    @Override
    public Circuit<? extends Interface<CorsaInterface>> resolve(int label) {
        return this.tag(TagKind.VLAN_STAG, label >> 12)
            .circuit(label & 0xfff);
    }
}
