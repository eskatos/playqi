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
import org.qi4j.api.structure.Layer;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.spi.Qi4jSPI;

public class PlayQiPlugin
        extends play.Plugin
{

    private static final String CONFIG_APP_ASSEMBLER = "qi4j.app-assembler";

    private final play.Application play2app;

    private Energy4Java qi4j;

    private Application application;

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

        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( play2app.classloader() );

        String appAssClassName = play2app.configuration().getString( CONFIG_APP_ASSEMBLER );

        try {

            Class<ApplicationAssembler> appAssClass = ( Class<ApplicationAssembler> ) play2app.classloader().loadClass( appAssClassName );
            ApplicationAssembler appass = appAssClass.newInstance();

            qi4j = ( Energy4Java ) play2app.classloader().loadClass( Energy4Java.class.getName() ).newInstance();
            application = qi4j.newApplication( appass );
            application.activate();

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
            Thread.currentThread().setContextClassLoader( originalClassLoader );
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
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
