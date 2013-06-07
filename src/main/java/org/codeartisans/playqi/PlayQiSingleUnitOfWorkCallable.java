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

import java.util.concurrent.Callable;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;

@Deprecated
public class PlayQiSingleUnitOfWorkCallable<T>
    implements Callable<T>
{

    private final Callable<T> delegate;

    public PlayQiSingleUnitOfWorkCallable( Callable<T> delegate )
    {
        this.delegate = delegate;
    }

    @Override
    public final T call()
        throws Exception
    {
        Module module = PlayQiSingle.module();
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            T result = delegate.call();
            uow.complete();
            return result;
        }
        catch( Exception ex )
        {
            if( uow != null )
            {
                uow.discard();
            }
            throw ex;
        }
    }
}
