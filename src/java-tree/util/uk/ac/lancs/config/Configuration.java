/*
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
package uk.ac.lancs.config;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Views a set of named configuration properties. Property names are the
 * same as for Java properties files.
 * 
 * <p>
 * Subviews of a configuration are obtainable. For example, if the
 * subview <samp>foo.bar</samp> is obtained, then only properties whose
 * names in the original view begin with <samp>foo.bar.</samp> will be
 * visible. Furthermore, their names will lack the prefix
 * <samp>foo.bar.</samp>.
 * 
 * @author simpsons
 */
public interface Configuration {
    /**
     * Get a configuration parameter.
     * 
     * @param key the parameter key
     * 
     * @return the parameter's value, or {@code null} if not present
     */
    String get(String key);

    /**
     * Get a configuration parameter, or a default.
     * 
     * @param key the parameter key
     * 
     * @param defaultValue the value to return if the parameter is not
     * set
     * 
     * @return the parameter's value, or <samp>defaultValue</samp> if
     * not set
     */
    default String get(String key, String defaultValue) {
        String value = get(key);
        if (value == null) return defaultValue;
        return value;
    }

    /**
     * Obtain a locally referenced configuration.
     * 
     * @param key the key used as a base for resolving a relative name
     * 
     * @param value the reference to the configuration
     * 
     * @return the referenced configuration
     * 
     * @throws NullPointerException if <samp>value</samp> is
     * {@code null}
     */
    Configuration reference(String key, String value);

    /**
     * Convert the properties in this configuration into a conventional
     * Java properties object.
     * 
     * @return a copy of this configuration's properties
     */
    default Properties toProperties() {
        Properties result = new Properties();
        for (String key : keys())
            result.setProperty(key, get(key));
        return result;
    }

    /**
     * Obtain a locally referenced configuration, or a default if not
     * specified.
     * 
     * @param key the key used to obtain the reference, and as a base
     * for resolving a relative name
     * 
     * @param defaultValue the configuration to return if not found
     * 
     * @return the referenced configuration, or the default
     */
    default Configuration reference(String key, Configuration defaultValue) {
        String value = get(key);
        if (value == null) return defaultValue;
        return reference(key, value);
    }

    /**
     * Obtain a locally referenced configuration if specified.
     * 
     * @param key the key used to obtain the reference, and as a base
     * for resolving a relative name
     * 
     * @return the referenced configuration, or {@code null} if not
     * specified
     */
    default Configuration reference(String key) {
        return reference(key, (Configuration) null);
    }

    /**
     * Get a subview.
     * 
     * @param prefix the additional prefix to narrow down the available
     * parameters
     * 
     * @return the requested subview
     */
    Configuration subview(String prefix);

    /**
     * List keys in this configuration.
     * 
     * @return the keys
     */
    Iterable<String> keys();

    /**
     * List a subset of keys.
     * 
     * @param condition a condition selecting keys to include
     * 
     * @return the selected keys
     */
    default Iterable<String>
        selectedKeys(Predicate<? super String> condition) {
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new FilterIterator<>(keys().iterator(), condition);
            }
        };
    }

    /**
     * List transformed keys.
     * 
     * @param <K> the type to transform keys into
     * 
     * @param xform the transformation function
     * 
     * @return the transformed keys
     */
    default <K> Iterable<K>
        transformedKeys(Function<? super String, ? extends K> xform) {
        return new Iterable<K>() {
            @Override
            public Iterator<K> iterator() {
                return new TransformIterator<>(keys().iterator(), xform);
            }
        };
    }

    /**
     * List selected and then transformed keys.
     * 
     * @param precondition a condition selecting keys to include
     * 
     * @param <K> the type to transform keys into
     * 
     * @param xform the transformation function
     * 
     * @return the selected and then transformed keys
     */
    default <K> Iterable<K>
        transformedSelectedKeys(Predicate<? super String> precondition,
                                Function<? super String, ? extends K> xform) {
        return new Iterable<K>() {
            @Override
            public Iterator<K> iterator() {
                return new TransformIterator<>(new FilterIterator<>(keys()
                    .iterator(), precondition), xform);
            }
        };
    }

    /**
     * List a selection of transformed keys.
     * 
     * @param <K> the type to transform keys into
     * 
     * @param xform the transformation function
     * 
     * @param postcondition a condition selecting transformed keys
     * 
     * @return the selected transformed keys
     */
    default <K> Iterable<K>
        selectedTransformedKeys(Function<? super String, ? extends K> xform,
                                Predicate<? super K> postcondition) {
        return new Iterable<K>() {
            @Override
            public Iterator<K> iterator() {
                return new FilterIterator<>(new TransformIterator<>(keys()
                    .iterator(), xform), postcondition);
            }
        };
    }

    /**
     * List a selection of transformed selected keys.
     * 
     * @param precondition a condition selecting keys to include
     * 
     * @param <K> the type to transform keys into
     * 
     * @param xform the transformation function
     * 
     * @param postcondition a condition selecting transformed keys
     * 
     * @return the selected transformed keys
     */
    default <K> Iterable<K>
        selectedTransformedSelectedKeys(Predicate<? super String> precondition,
                                        Function<? super String, ? extends K> xform,
                                        Predicate<? super K> postcondition) {
        return new Iterable<K>() {
            @Override
            public Iterator<K> iterator() {
                return new FilterIterator<>(new TransformIterator<>(new FilterIterator<>(keys()
                    .iterator(), precondition), xform), postcondition);
            }
        };
    }

    /**
     * Normalize a node key. Double dots are condensed to single ones.
     * Leading and trailing dots are removed.
     * 
     * @param key the node key to normalize
     * 
     * @return the normalized node key
     */
    public static String normalizeKey(String key) {
        if (key == null) return null;
        List<String> parts =
            new LinkedList<>(Arrays.asList(key.split("\\.+")));
        for (ListIterator<String> iter = parts.listIterator(); iter
            .hasNext();) {
            String val = iter.next();
            if (val.isEmpty()) iter.remove();
        }
        return parts.stream().collect(Collectors.joining("."));
    }

    /**
     * Normalize a node key prefix.
     * 
     * @param prefix the node key prefix to normalize
     * 
     * @return the normalized prefix
     */
    public static String normalizePrefix(String prefix) {
        return prefix == null ? null : normalizeKey(prefix) + '.';
    }

    /**
     * Resolve a potentially relative key against a base. If the key
     * does not begin with a <samp>.</samp>, it is returned unchanged.
     * Otherwise, everything after the last <samp>.</samp> in the base
     * is removed, and the key is appended.
     * 
     * @param base the base to resolve the key against
     * 
     * @param key the key to resolve
     * 
     * @return
     */
    public static String resolveKey(String base, String key) {
        if (key.isEmpty()) return "";
        if (key.charAt(0) != '.') return key;
        int lastDot = base.lastIndexOf('.');
        if (lastDot < 0) return key.substring(1);
        return base.substring(0, lastDot) + key;
    }
}
