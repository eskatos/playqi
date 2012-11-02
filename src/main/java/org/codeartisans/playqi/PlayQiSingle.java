/*
 * Copyright 2012 Paul Merlin.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codeartisans.playqi;

import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Layer;
import org.qi4j.api.structure.Module;

/**
 * PlayQi API for a Qi4j Application assembled using SingletonAssembler.
 */
public class PlayQiSingle
{

    public static final String LAYER = "Layer 1";
    public static final String MODULE = "Module 1";

    /**
     * @return The Application represents a whole Qi4j application.
     */
    public static Application application()
    {
        return play.Play.application().plugin( PlayQiPlugin.class ).application();
    }

    /**
     * @return The Layer represents a single layer in a Qi4j application.
     */
    public static Layer layer()
    {
        return application().findLayer( LAYER );
    }

    /**
     * @return API for interacting with a Module.
     */
    public static Module module()
    {
        return application().findModule( LAYER, MODULE );
    }

    /**
     * From a ServiceReference you can access and modify metadata about a service.
     * You can also access the actual service through get(), that can then be invoked.
     *
     * @param <T> the type that the Service must implement
     * @param serviceType the type that the Service must implement
     * @return all the ServiceReferences matching the given Service type
     */
    public static <T> Iterable<ServiceReference<T>> servicesReferences( Class<T> servicetype )
    {
        return module().findServices( servicetype );
    }

    /**
     * From a ServiceReference you can access and modify metadata about a service.
     * You can also access the actual service through get(), that can then be invoked.
     *
     * @param <T> the type that the Service must implement
     * @param serviceType the type that the Service must implement
     * @return the ServiceReference
     */
    public static <T> ServiceReference<T> serviceReference( Class<T> serviceType )
    {
        return module().findService( serviceType );
    }

    /**
     * @param <T> the type that the Service must implement
     * @param serviceType the type that the Service must implement
     * @return the service instance
     */
    public static <T> T service( Class<T> serviceType )
    {
        return module().findService( serviceType ).get();
    }

    public static <T> T newControllerInstance( Class<T> controllerType )
    {
        return module().newObject( controllerType );
    }

    public static <T> T newTransientControllerInstance( Class<T> controllerType )
    {
        return module().newTransient( controllerType );
    }

    protected PlayQiSingle()
    {
    }

}
