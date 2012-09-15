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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.qi4j.api.Qi4j;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Application.Mode;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.api.structure.Layer;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Qi4jRuntime;
import org.qi4j.bootstrap.RuntimeFactory;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.envisage.Envisage;
import org.qi4j.library.swing.entityviewer.EntityViewer;
import org.qi4j.spi.Qi4jSPI;

public class PlayQiPlugin
        extends play.Plugin
{

    private static final String ENABLED = "enabled";

    private static final String CONFIG_APP_ASSEMBLER = "qi4j.app-assembler";

    private static final String CONFIG_ENVISAGE = "qi4j.envisage";

    private static final String CONFIG_ENTITY_VIEWER = "qi4j.entity-viewer";

    private static final String CONFIG_INJECT_LAYER = "qi4j.inject.layer";

    private static final String CONFIG_INJECT_MODULE = "qi4j.inject.module";

    private static final String CONFIG_INJECT_PACKAGES = "qi4j.inject.packages";

    private final play.Application play2app;

    private Qi4jRuntime qi4j;

    private Application application;

    private Envisage envisageInstance;

    private EntityViewer entityViewerInstance;

    public PlayQiPlugin( play.Application play2app )
    {
        this.play2app = play2app;
    }

    public Application application()
    {
        return application;
    }

    public Qi4j api()
    {
        return qi4j.api();
    }

    public Qi4jSPI spi()
    {
        return qi4j.spi();
    }

    @Override
    public void onStart()
    {
        super.onStart();

        ClassLoader classloader = play2app.classloader();
        play.Configuration configuration = play2app.configuration();

        ClassLoader originalClassloader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( classloader );


        String appAssClassName = configuration.getString( CONFIG_APP_ASSEMBLER );
        boolean envisage = ENABLED.equals( configuration.getString( CONFIG_ENVISAGE ) );
        boolean entityViewer = ENABLED.equals( configuration.getString( CONFIG_ENTITY_VIEWER ) );
        String injectLayer = configuration.getString( CONFIG_INJECT_LAYER );
        String injectModule = configuration.getString( CONFIG_INJECT_MODULE );
        String injectPackages = configuration.getString( CONFIG_INJECT_PACKAGES );
        injectPackages = injectPackages == null ? "controllers" : injectPackages;

        Mode mode = play2app.isProd() ? Mode.production : play2app.isDev() ? Mode.development : Mode.test;

        try {

            Class<ApplicationAssembler> appAssClass = ( Class<ApplicationAssembler> ) classloader.loadClass( appAssClassName );
            ApplicationAssembler assembler = appAssClass.newInstance();

            if ( assembler instanceof SingletonAssembler ) {
                injectLayer = PlayQiSingle.LAYER;
                injectModule = PlayQiSingle.MODULE;
            }

            RuntimeFactory runtimeFactory = ( RuntimeFactory ) classloader.loadClass(
                    RuntimeFactory.StandaloneApplicationRuntimeFactory.class.getName() ).newInstance();
            qi4j = runtimeFactory.createRuntime();
            if ( qi4j == null ) {
                throw new PlayQiException( "Can not create Qi4j without a Qi4j Runtime." );
            }

            ApplicationAssembly assembly = assembler.assemble( qi4j.applicationAssemblyFactory() );
            if ( assembly == null ) {
                throw new PlayQiException( "Application assembler did not return any ApplicationAssembly" );
            }

            // Set Play mode to Qi4j mode
            assembly.setMode( mode );

            ApplicationDescriptor model = qi4j.applicationModelFactory().newApplicationModel( assembly );
            application = model.newInstance( qi4j.api() );
            application.activate();

            // Development tools
            if ( mode == Mode.development ) {
                if ( envisage ) {
                    envisageInstance = ( Envisage ) classloader.loadClass( Envisage.class.getName() ).newInstance();
                    envisageInstance.run( model );
                }
                if ( entityViewer ) {
                    entityViewerInstance = ( EntityViewer ) classloader.loadClass( EntityViewer.class.getName() ).newInstance();
                    entityViewerInstance.show( qi4j.spi(), model, application );
                }
            }

            if ( injectLayer != null && injectModule != null ) {
                for ( String pkg : injectPackages.split( ":" ) ) {
                    Class[] classes = Helper.getClasses( pkg, classloader );
                    for ( Class clazz : classes ) {
                        eventuallyInjectStatic( clazz, injectLayer, injectModule );
                    }
                }
            }

            play.Logger.debug( "Qi4jPlugin started!" );

        } catch ( ClassNotFoundException ex ) {
            throw new PlayQiException( "Unable to find Qi4j ApplicationAssembler: " + ex.getMessage(), ex );
        } catch ( ClassCastException ex ) {
            throw new PlayQiException( appAssClassName + " is not a Qi4j ApplicationAssembler: " + ex.getMessage(), ex );
        } catch ( InstantiationException ex ) {
            throw new PlayQiException( "Unable to instanciate " + appAssClassName + ": " + ex.getMessage(), ex );
        } catch ( IllegalAccessException ex ) {
            throw new PlayQiException( "Unable to instanciate " + appAssClassName + ": " + ex.getMessage(), ex );
        } catch ( AssemblyException ex ) {
            throw new PlayQiException( "Unable to assemble Qi4j Application: " + ex.getMessage(), ex );
        } catch ( Exception ex ) {
            throw new PlayQiException( "Unable to activate Qi4j Application: " + ex.getMessage(), ex );
        } finally {
            Thread.currentThread().setContextClassLoader( originalClassloader );
        }
    }

    private void eventuallyInjectStatic( Class clazz, String injectLayer, String injectModule )
    {
        // QUID What about Scala support?
        Layer layer = application.findLayer( injectLayer );
        Module module = application.findModule( injectLayer, injectModule );
        try {
            for ( Field field : clazz.getDeclaredFields() ) {
                if ( Modifier.isStatic( field.getModifiers() ) ) {
                    if ( field.isAnnotationPresent( Structure.class ) ) {

                        if ( field.getType().isAssignableFrom( Qi4jSPI.class )
                             || field.getType().isAssignableFrom( Qi4j.class ) ) {

                            inject( field, qi4j );

                        } else if ( field.getType().isAssignableFrom( Application.class ) ) {

                            inject( field, application );

                        } else if ( field.getType().isAssignableFrom( Layer.class ) ) {

                            inject( field, layer );

                        } else if ( field.getType().isAssignableFrom( Module.class )
                                    || field.getType().isAssignableFrom( TransientBuilderFactory.class )
                                    || field.getType().isAssignableFrom( UnitOfWorkFactory.class )
                                    || field.getType().isAssignableFrom( ServiceFinder.class ) ) {

                            inject( field, module );

                        }

                    } else if ( field.isAnnotationPresent( Service.class ) ) {

                        if ( field.getType().isAssignableFrom( Iterable.class ) ) {

                            inject( field, module.findServices( field.getType() ) );

                        } else if ( field.getType().isAssignableFrom( ServiceReference.class ) ) {

                            inject( field, module.findService( field.getType() ) );

                        } else {

                            inject( field, module.findService( field.getType() ).get() );

                        }

                    }
                }
            }
        } catch ( Exception ex ) {
            throw new PlayQiException( "Unable to inject from '" + injectLayer + "/" + injectModule
                                       + "' to '" + clazz.getName() + "': " + ex.getMessage(), ex );
        }
    }

    private void inject( Field field, Object value )
            throws IllegalArgumentException, IllegalAccessException
    {
        field.setAccessible( true );
        field.set( null, value );
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if ( envisageInstance != null ) {
            envisageInstance.stop();
        }
        if ( entityViewerInstance != null ) {
            entityViewerInstance.stop();
        }
        try {
            application.passivate();
        } catch ( Exception ex ) {
            play.Logger.warn( "An exception occured during Qi4j Application passivation: " + ex.getMessage(), ex );
        } finally {
            application = null;
            qi4j = null;
        }
        play.Logger.debug( "Qi4jPlugin stopped!" );
    }

    @Override
    public boolean enabled()
    {
        return play2app.configuration().getString( CONFIG_APP_ASSEMBLER ) != null;
    }

}
