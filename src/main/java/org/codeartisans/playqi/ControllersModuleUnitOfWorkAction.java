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

import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;

public class ControllersModuleUnitOfWorkAction
        extends Action<ControllersModuleUnitOfWorkConcern>
{

    @Override
    public Result call( Context context )
            throws Throwable
    {
        Module module = PlayQi.controllersModule();
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            Result result = delegate.call( context );
            uow.complete();
            return result;
        }
        catch( UnitOfWorkCompletionException ex )
        {
            throw ex;
        }
        catch( Throwable ex )
        {
            if( uow != null )
            {
                uow.discard();
            }
            throw ex;
        }
    }

}
