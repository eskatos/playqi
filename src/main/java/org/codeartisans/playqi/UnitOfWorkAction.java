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

import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import play.Play;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public class UnitOfWorkAction
        extends Action<UnitOfWorkPlayConcern>
{

    @Override
    public Result call( Http.Context context )
            throws Throwable
    {
        String layerName = configuration.layer();
        String moduleName = configuration.module();
        Application application = Play.application().plugin( PlayQiPlugin.class ).application();
        Module module = application.findModule( layerName, moduleName );
        UnitOfWork uow = module.newUnitOfWork();
        try {
            Result result = delegate.call( context );
            uow.complete();
            uow = null;
            return result;
        } finally {
            if ( uow != null ) {
                uow.discard();
            }
        }
    }

}