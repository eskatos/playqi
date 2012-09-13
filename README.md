# Play! 2 Qi4j Plugin

This plugin ties a Qi4j Application to a Play! 2 Application providing tight
integration.

## What is Play!?

> The Play framework makes it easier to build web applications with Java &
> Scala.

> Play is based on a lightweight, stateless, web-friendly architecture and
> features predictable and minimal resource consumption (CPU, memory, threads)
> for highly-scalable applications - thanks to its reactive model, based on
> Iteratee IO.

Official Site  
[http://playframework.org/](http://playframework.org/)

Questions & Answers  
[http://stackoverflow.com/questions/tagged/playframework-2.0]
(http://stackoverflow.com/questions/tagged/playframework-2.0)

Discussion  
[http://groups.google.com/group/play-framework]
(http://groups.google.com/group/play-framework)

## What is Qi4j?

> The short answer is that Qi4j is a framework for domain centric application
> development, including evolved concepts from AOP, DI and DDD.

> Qi4j is an implementation of Composite Oriented Programming, using the
> standard Java 5 platform, without the use of any pre-processors or new
> language elements. Everything you know from Java 5 still applies and you can
> leverage both your experience and toolkits to become more productive with
> Composite Oriented Programming today.

> Moreover, Qi4j enables Composite Oriented Programming on the Java platform,
> including both Java and Scala as primary languages as well as many of the
> plethora of languages running on the JVM as bridged languages.

Official Site  
[http://qi4j.org/](http://qi4j.org/)

Questions & Answers  
[http://stackoverflow.com/questions/tagged/qi4j]
(http://stackoverflow.com/questions/tagged/qi4j)

Discussion  
[http://qi4j.org/help.html](http://qi4j.org/help.html)

As Qi4j itself is written in the Java language, this Play plugin is too.
Sample code you'll find below is __Java__ code but all this works in Play
__Scala__ applications.

You can use Qi4j for a small part of your application where Composite Oriented
Programming fits or go for a bigger DDD stack using Qi4j
[Libraries](http://qi4j.org/libraries.html) and
[Extensions](http://qi4j.org/extensions.html). In development mode the Qi4j
[Tools](http://qi4j.org/tools.html) can come in handy.

## Installation

* Add ````https://oss.sonatype.org/content/repositories/snapshots/```` and ````https://repository-qi4j.forge.cloudbees.com/snapshot/```` repositories as resolvers to your ````project/Build.scala```` ;
* add ````"org.codeartisans" %% "playqi" % "1.0-SNAPSHOT"```` and ````"org.qi4j.core" %% "org.qi4j.core.runtime" % "2.0-SNAPSHOT"```` to your dependencies in ````project/Build.scala```` ;
* add ````1500:org.codeartisans.playqi.PlayQiPlugin```` to your  ````conf/play.plugins````.

## Configuration

The plugin request that you set the ````qi4j.app-assembler```` parameter in ````conf/application.conf````. 

A Qi4j application is assembled using an __ApplicationAssembler__, set
````qi4j.app-assembler```` to the fully qualified name of yours class.

As a quick start and for a simple Qi4j application you can extend
SingletonAssembler ;

    public class MyAppAssembler extends SingletonAssembler {
      public void assemble(ModuleAssembly ma) throws AssemblyException {
        ma.values( Comment.class, Tagline.class );
        ma.entities( Post.class, Page.class );
        ma.services( Blog.class
                     MemoryEntityStoreService.class, 
                     UuidIdentityGeneratorService.class );
      }
    }

and use ````qi4j.app-assembler=bootstrap.MyAppAssembler````.

See this tutorial on [how to assemble a more complete Qi4j application]
(http://qi4j.org/howto-assemble-application.html).

## Integrations

### Lifecycle

Play and Qi4j Applications are __started__ / __activated__ and __passivated__
/ __stopped__ together

### Compose Play Actions with Qi4j UnitOfWorks

This plugin provides the ````@UnitOfWorkActionConcern```` annotation to wrap Qi4j
UnitsOfWork around Play Actions.

    import static org.codeartisans.playqi.PlayQiSingle.*;
    @UnitOfWorkActionConcern( LAYER, MODULE )
    public static Result action() {
      :
      return ok( .. );
    }

### Plugin API

    // For a simple Qi4j application based on SingletonAssembler:
    Application app = PlayQiSingle.application();
    Layer layer = PlayQiSingle.layer();
    Module module = PlayQiSingle.module();
    Blog blog = PlayQiSingle.service( Blog.class );

    // For a Qi4j application using layers and modules:
    Application app = PlayQi.application();
    Layer layer = PlayQi.layer( "Presentation" );
    Module module = PlayQi.module( "Presentation", "Contexts );
    Blog blog = PlayQi.service( "Presentation", "Contexts, Blog.class );

Here is a simple exemple using a SingletonAssembler:

    import static org.codeartisans.playqi.PlayQiSingle.*;
    public class BlogController extends Controller {
      public static Result index() {
        return ok( template.render( service( Blog.class ).homepage() ) );
      }
    }


## Licence

This software is licensed under the Apache 2 license, quoted below.

Copyright 2012 Paul Merlin.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use
this project except in compliance with the License. You may obtain a copy of
the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.

