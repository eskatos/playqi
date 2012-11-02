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
 * PlayQi API for a Qi4j Application using a custom Assembler.
 */
public class PlayQi
{

    /**
     * @return The Application represents a whole Qi4j application.
     */
    public static Application application()
    {
        return play.Play.application().plugin( PlayQiPlugin.class ).application();
    }

    /**
     * @param layerName the name of the Layer
     * @return The Layer represents a single layer in a Qi4j application.
     */
    public static Layer layer( String layerName )
    {
        return application().findLayer( layerName );
    }

    /**
     * @param layerName the name of the Layer
     * @param moduleName the name of the Module
     * @return API for interacting with a Module.
     */
    public static Module module( String layerName, String moduleName )
    {
        return application().findModule( layerName, moduleName );
    }

    /**
     * From a ServiceReference you can access and modify metadata about a service.
     * You can also access the actual service through get(), that can then be invoked.
     *
     * @param <T> the type that the Service must implement
     * @param layerName the name of the Layer
     * @param moduleName the name of the Module
     * @param serviceType the type that the Service must implement
     * @return all the ServiceReferences matching the given Service type
     */
    public static <T> Iterable<ServiceReference<T>> servicesReferences( String layerName, String moduleName, Class<T> serviceType )
    {
        return module( layerName, moduleName ).findServices( serviceType );
    }

    /**
     * From a ServiceReference you can access and modify metadata about a service.
     * You can also access the actual service through get(), that can then be invoked.
     *
     * @param <T> the type that the Service must implement
     * @param layerName the name of the Layer
     * @param moduleName the name of the Module
     * @param serviceType the type that the Service must implement
     * @return the ServiceReference
     */
    public static <T> ServiceReference<T> serviceReference( String layerName, String moduleName, Class<T> serviceType )
    {
        return module( layerName, moduleName ).findService( serviceType );
    }

    /**
     * @param <T> the type that the Service must implement
     * @param layerName the name of the Layer
     * @param moduleName the name of the Module
     * @param serviceType the type that the Service must implement
     * @return the Service instance
     */
    public static <T> T service( String layerName, String moduleName, Class<T> serviceType )
    {
        return module( layerName, moduleName ).findService( serviceType ).get();
    }

    /**
     * @return the layer configured as qi4j.controllers-layer
     */
    public static Layer controllersLayer()
    {
        return play.Play.application().plugin( PlayQiPlugin.class ).controllersLayer();
    }

    /**
     * @return the module configured as qi4j.controllers-module
     */
    public static Module controllersModule()
    {
        return play.Play.application().plugin( PlayQiPlugin.class ).controllersModule();
    }

    /**
     * Find a ServiceReference in the module configured as qi4j.inject.module.
     *
     * @param <T> the type that the Service must implement
     * @param serviceType the type that the Service must implement
     * @return all the ServiceReferences matching the given Service type
     */
    public static <T> Iterable<ServiceReference<T>> servicesReferences( Class<T> serviceType )
    {
        return controllersModule().findServices( serviceType );
    }

    /**
     * Find a ServiceReference in the module configured as qi4j.inject.module.
     *
     * @param <T> the type that the Service must implement
     * @param serviceType the type that the Service must implement
     * @return the ServiceReference
     */
    public static <T> ServiceReference<T> serviceReference( Class<T> serviceType )
    {
        return controllersModule().findService( serviceType );
    }

    /**
     * Find a Service instance in the module configured as qi4j.inject.module.
     *
     * @param <T> the type that the Service must implement
     * @param serviceType the type that the Service must implement
     * @return the Service instance
     */
    public static <T> T service( Class<T> serviceType )
    {
        return controllersModule().findService( serviceType ).get();
    }

    protected PlayQi()
    {
    }

}
