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

public class PlayQiException
        extends RuntimeException
{

    public PlayQiException( String string )
    {
        super( string );
    }

    public PlayQiException( Throwable thrwbl )
    {
        super( thrwbl.getMessage(), thrwbl );
    }

    public PlayQiException( String string, Throwable thrwbl )
    {
        super( string, thrwbl );
    }

}
