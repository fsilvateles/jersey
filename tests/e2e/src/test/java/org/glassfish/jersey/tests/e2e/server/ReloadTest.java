/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.jersey.tests.e2e.server;


import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.AbstractContainerLifecycleListener;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Reload capability test.
 *
 * For jdk http server test container, run with:
 *
 * mvn -Dtest=ReloadTest -Djersey.config.test.container.factory=org.glassfish.jersey.test.jdkhttp.JdkHttpServerTestContainerFactory clean test
 *
 * @author Paul Sandoz (paul.sandoz at oracle.com)
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class ReloadTest extends JerseyTest {

    @Path("one")
    public static class One {
        @GET
        public String get() {
            return "one";
        }
    }

    @Path("two")
    public static class Two {
        @GET
        public String get() {
            return "two";
        }
    }

    private static class Reloader extends AbstractContainerLifecycleListener {
        Container container;


        public void reload(ResourceConfig rc) {
            container.reload(rc);
        }

        @Override
        public void onStartup(Container container) {
            this.container = container;
        }
    }

    ResourceConfig rc;
    Reloader reloader;

    private ResourceConfig _createRC(Reloader r) {
        final ResourceConfig result = new ResourceConfig(One.class);
        result.addSingletons(r);

        return result;
    }

    @Override
    public ResourceConfig configure() {
        reloader = new Reloader();
        return rc = _createRC(reloader);
    }


    @Test
    public void testReload() {

        assertEquals("one", target().path("one").request().get().readEntity(String.class));
        assertEquals(404, target().path("two").request().get().getStatus());

        rc = _createRC(reloader).addClasses(Two.class);
        reloader.reload(rc);

        assertEquals("one", target().path("one").request().get().readEntity(String.class));
        assertEquals("two", target().path("two").request().get().readEntity(String.class));
    }
}