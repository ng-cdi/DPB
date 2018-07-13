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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.json.simple.parser.ParseException;

import uk.ac.lancs.agent.Agent;
import uk.ac.lancs.agent.AgentContext;
import uk.ac.lancs.agent.AgentCreationException;
import uk.ac.lancs.agent.AgentFactory;
import uk.ac.lancs.agent.CacheAgent;
import uk.ac.lancs.agent.ServiceCreationException;
import uk.ac.lancs.config.Configuration;
import uk.ac.lancs.networks.fabric.Fabric;
import uk.ac.lancs.scc.jardeps.Service;

/**
 * Creates agents each implementing a Corsa DP2X00-compatible fabric.
 * One type of {@link Fabric} service is implemented, namely
 * {@link VFCPerServiceFabric}, recognized by a configuration parameter
 * called <samp>{@value #TYPE_FIELD}</samp> with the value
 * <samp>{@value #VFCPERSERVICE_TYPE_NAME}</samp>.
 * 
 * <table>
 * <thead>
 * <tr>
 * <th>Value of <samp>{@value #TYPE_FIELD}</samp></th>
 * <th>Implementation</th>
 * </tr>
 * </thead>
 * 
 * <tbody>
 * <tr>
 * <td><samp>{@value #VFCPERSERVICE_TYPE_NAME}</samp></td>
 * <td>{@link VFCPerServiceFabric}</td>
 * </tr>
 * </tbody>
 * </table>
 * 
 * <p>
 * The following configuration properties are recognized:
 * 
 * <dl>
 * 
 * <dt><samp>capacity.ports</samp> (default
 * {@value #DEFAULT_PORT_COUNT})
 * 
 * <dd>Specifies the number of ports in the switch.
 * 
 * <dt><samp>capacity.lags</samp> (default: half of
 * <samp>capacity.ports</samp>)
 * 
 * <dd>Specifies the highest numbered link aggregation group in the
 * switch.
 * 
 * <dt><samp>capacity.bridges</samp> (default
 * {@value #DEFAULT_MAX_BRIDGES})
 * 
 * <dd>Specifies the maximum number of bridges to be created.
 * 
 * <dt><samp>description.prefix</samp> (default
 * <samp>{@value #DEFAULT_DESCRIPTION_PREFIX}</samp>)
 * <dt><samp>description.partial</samp> (default
 * <samp>{@value #DEFAULT_PARTIAL_DESCRIPTION_SUFFIX}</samp>)
 * <dt><samp>description.complete</samp> (default
 * <samp>{@value #DEFAULT_COMPLETE_DESCRIPTION_SUFFIX}</samp>)
 * 
 * <dd>Specify the strings used in the text descriptions of each bridge.
 * The partial text is used on creation, and the complete text is used
 * when the bridge is fully configured and operational. Bridges left in
 * the partial state due to error or early termination will be removed
 * on the next restart of the agent. Any other bridge with the same
 * prefix is considered to be the responsibility of the agent, assumed
 * to be related to a different fabric implementation that it has
 * replaced, and will also be removed.
 * 
 * <dt><samp>subtype</samp> (default
 * <samp>{@value #DEFAULT_SUBYTPE}</samp>)
 * 
 * <dd>Specifies the type of VFC to create.
 * 
 * <dt><samp>ctrl.netns</samp> (default
 * <samp>{@value #DEFAULT_NETWORK_NAMESPACE}</samp>!)
 * 
 * <dd>Specifies the network namespace for the controller port of the
 * new bridges.
 * 
 * <dt><samp>ctrl.host</samp> (default
 * <samp>{@value #DEFAULT_CONTROLLER_HOST}</samp>)
 * <dt><samp>ctrl.port</samp> (default
 * <samp>{@value #DEFAULT_CONTROLLER_PORT}</samp>)
 * 
 * <dd>Specify the controller address used to configure each bridge
 * with. The defaults point to one of the local learning-switch
 * controllers.
 * 
 * <dt><samp>rest.location</samp>
 * 
 * <dd>Specifies the URI of the switch's REST API. Do not include
 * <samp>api/v1/</samp> components, as these are added automatically.
 * 
 * <dt><samp>rest.cert.file</samp>
 * 
 * <dd>Specifies the filename (as a URI relative to the containing file)
 * containing an X.509 certificate used to verify the identity of the
 * Corsa switch.
 * 
 * <dt><samp>rest.authz.file</samp>
 * 
 * <dd>Specifies the filename (as a URI relative to the containing file)
 * containing the authorization string to be sent with each REST
 * request. The string may be broken into several lines for readability.
 * 
 * </dl>
 * 
 * @author simpsons
 */
@Service(AgentFactory.class)
public class DP2000FabricAgentFactory implements AgentFactory {
    /**
     * @undocumented
     */
    public static final String DEFAULT_DESCRIPTION_PREFIX = "initiate:vc:";

    /**
     * @undocumented
     */
    public static final String DEFAULT_PARTIAL_DESCRIPTION_SUFFIX = "partial";

    /**
     * @undocumented
     */
    public static final String DEFAULT_COMPLETE_DESCRIPTION_SUFFIX =
        "complete";

    /**
     * @undocumented
     */
    public static final String DEFAULT_NETWORK_NAMESPACE = "default";

    /**
     * @undocumented
     */
    public static final String DEFAULT_SUBYTPE = "l2-vpn";

    /**
     * @undocumented
     */
    public static final String VFCPERSERVICE_TYPE_NAME =
        "corsa-dp2x00-brperlink";

    /**
     * @undocumented
     */
    public static final String DEFAULT_CONTROLLER_HOST = "172.17.1.1";

    /**
     * @undocumented
     */
    public static final int DEFAULT_CONTROLLER_PORT = 6653;

    /**
     * @undocumented
     */
    public static final String TYPE_FIELD = "type";

    /**
     * @undocumented
     */
    public static final int DEFAULT_PORT_COUNT = 32;

    /**
     * @undocumented
     */
    public static final int DEFAULT_MAX_BRIDGES = 63;

    /**
     * {@inheritDoc}
     * 
     * @default This implementation recognizes only the string
     * <samp>{@value #VFCPERSERVICE_TYPE_NAME}</samp> in the field
     * <samp>{@value #TYPE_FIELD}</samp>.
     */
    @Override
    public boolean recognize(Configuration conf) {
        String type = conf.get(TYPE_FIELD);
        return VFCPERSERVICE_TYPE_NAME.equals(type);
    }

    @Override
    public Agent makeAgent(AgentContext ctxt, Configuration conf)
        throws AgentCreationException {
        final String descPrefix =
            conf.get("description.prefix", DEFAULT_DESCRIPTION_PREFIX);
        final String partialDescSuffix = conf
            .get("description.partial", DEFAULT_PARTIAL_DESCRIPTION_SUFFIX);
        final String fullDescSuffix = conf
            .get("description.complete", DEFAULT_COMPLETE_DESCRIPTION_SUFFIX);
        final String subtype = conf.get("subtype", DEFAULT_SUBYTPE);
        final String netns =
            conf.get("ctrl.netns", DEFAULT_NETWORK_NAMESPACE);
        final InetSocketAddress controller =
            new InetSocketAddress(conf.get("ctrl.host",
                                           DEFAULT_CONTROLLER_HOST),
                                  Integer
                                      .parseInt(conf.get("ctrl.port", Integer
                                          .toString(DEFAULT_CONTROLLER_PORT))));
        final int maxBridges = Integer.parseInt(conf
            .get("capacity.bridges", Integer.toString(DEFAULT_MAX_BRIDGES)));
        final int portCount = Integer.parseInt(conf
            .get("capacity.ports", Integer.toString(DEFAULT_PORT_COUNT)));
        final int maxAggregations = Integer.parseInt(conf
            .get("capacity.lags", Integer.toString(portCount / 2)));
        final URI service = URI.create(conf.get("rest.location"));
        final File certFile = conf.getFile("rest.cert.file");
        final X509Certificate cert;
        try (InputStream in = new FileInputStream(certFile)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) cf.generateCertificate(in);
        } catch (IOException | CertificateException e) {
            throw new AgentCreationException("getting certificate from "
                + certFile, e);
        }
        final Path authzFile = conf.getPath("rest.authz.file");
        final String authz;
        try {
            authz = Files.readAllLines(authzFile, StandardCharsets.US_ASCII)
                .stream().collect(Collectors.joining());
        } catch (IOException e) {
            throw new AgentCreationException("getting authorization from "
                + authzFile, e);
        }
        return new CacheAgent(new Agent() {
            @Override
            public Collection<String> getKeys(Class<?> type) {
                if (type == Fabric.class) return Collections.singleton(null);
                return Collections.emptySet();
            }

            @Override
            public <T> T findService(Class<T> type, String key)
                throws ServiceCreationException {
                if (key != null) return null;
                if (type != Fabric.class) return null;
                try {
                    VFCPerServiceFabric result =
                        new VFCPerServiceFabric(portCount, maxAggregations,
                                                maxBridges, descPrefix,
                                                partialDescSuffix,
                                                fullDescSuffix, subtype,
                                                netns, controller, service,
                                                cert, authz);
                    result.init();
                    return type.cast(result);
                } catch (KeyManagementException | NoSuchAlgorithmException
                    | IOException | ParseException e) {
                    throw new ServiceCreationException(e);
                }
            }
        });
    }
}
