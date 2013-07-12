# PlayQi - a Play! 2 Qi4j Plugin

This plugin ties a Qi4j Application to a Play! >=2.1 Application providing a tight
integration between the two.

## What is Play!?

![](http://www.playframework.org/assets/images/logo.png "Play!")

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

![](http://qi4j.org/graphics/Qi4j-Logo-64x64.png "Qi4j")

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
[http://groups.google.com/group/qi4j-dev](http://groups.google.com/group/qi4j-dev)

As Qi4j itself is written in the Java language, this Play plugin is too.
Sample code you'll find below is __Java__ code but most of this works in Play
__Scala__ applications.

You can use Qi4j for a small part of your application where Composite Oriented
Programming fits or go for a bigger DDD stack using Qi4j
[Libraries](http://qi4j.org/latest/libraries.html) and
[Extensions](http://qi4j.org/latest/extensions.html). In development mode the Qi4j
[Tools](http://qi4j.org/latest/tools.html) can come in handy.


## How is "PlayQi" pronounced?

Qi4j is pronounced "chee for jay", so PlayQi is pronouced "play chee".


## Usage

### Installation

* Add ````https://oss.sonatype.org/content/repositories/snapshots/```` and ````https://repository-qi4j.forge.cloudbees.com/snapshot/```` repositories as resolvers to your ````project/Build.scala```` ;
* add ````"org.codeartisans" %% "playqi" % "1.1"```` and ````"org.qi4j.core" %% "org.qi4j.core.runtime" % "2.0"```` to your dependencies in ````project/Build.scala```` ;
* add ````1500:org.codeartisans.playqi.PlayQiPlugin```` to your  ````conf/play.plugins````.


### Application Assembly

The plugin request that you set the ````qi4j.app-assembler```` parameter in ````conf/application.conf````. 

A Qi4j application is assembled using an __ApplicationAssembler__, set ````qi4j.app-assembler```` to the fully qualified name of yours class.

As a quick start and for a simple Qi4j application you can extend SingletonAssembler ;

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
(http://qi4j.org/latest/howto-assemble-application.html).


## Integrations


### Application Lifecycle and Modes

Play and Qi4j Applications are __started__ / __activated__ and __passivated__
/ __stopped__ together.

Moreover, Play __DEV__ / __TEST__ / __PROD__ modes and Qi4j __development__ / __test__ / __production__ modes are synched.


### Plugin API

Depending on your Qi4j Application assembly you can use two APIs:

- ````PlayQiSingle```` for a simple Qi4j Application based on SingletonAssembler ;
- ````PlayQi```` for a Qi4j Application using layers and modules.

````PlayQiSingle```` is provided as an easy way to prototype or to integrate a small Qi4j application in a Play application. We recommend to use a true application assembly using layers and modules and the ````PlayQi```` API.

**PlayQiSingle**

When using a SingletonAssembler the single Module of the Qi4j Application is considered as the Module used by Play controllers, thus you don't need to set ````qi4j.controllers-layer```` nor ````qi4j.controllers-module```` in your configuration.

    Application app = PlayQiSingle.application();
    Layer layer = PlayQiSingle.layer();
    Module module = PlayQiSingle.module();
    Blog blog = PlayQiSingle.service( Blog.class );

Here is a simple exemple:

    public class BlogController extends Controller {
      public static Result index() {
        return ok( template.render( PlayQiSingle.service( Blog.class ).homepage() ) );
      }
    }

**PlayQi**

When using a complete Qi4j Application assembly you need to set ````qi4j.controllers-layer```` and ````qi4j.controllers-module```` in your configuration to point the Module of your Qi4j Application the Play controllers will use. Once done, ````PlayQi```` provide utility methods to quickly get handles on Qi4j composites:

    Application app = PlayQi.application();
    Layer controllersLayer = PlayQi.controllersLayer();
    Module controllersModule = PlayQi.controllersModule();
    Blog blog = PlayQi.service( Blog.class ); // From the controllers module

Besides, you can also get a handle on any layer/modules of your Qi4j Application:

    Layer layer = PlayQi.layer( "Presentation" );
    Module module = PlayQi.module( "Presentation", "Contexts" );
    Blog blog = PlayQi.service( "Presentation", "Contexts", Blog.class );


### Controllers injection

Qi4j Structure and Service injections scopes are supported in controllers.

> **WARNING** Controllers injection only work on Play! 2 **Java** Applications for now, we need help from Scala developers to implement this in **Scala**, any volunteers?

To use this facility you must set both ````qi4j.controllers-layer```` and ````qi4j.controllers-module````
parameters in your ````application.conf```` to define the Module from where the injections
will be done ;

    qi4j.controllers-layer=Presentation
    qi4j.controllers-module=Contexts

and then in your controllers:

    @Structure public static Application application;
    @Structure public static Layer layer;
    @Structure public static Module module;

    @Service public static Iterable<ServiceReference<Blog>> blogReferences;
    @Service public static ServiceReference<Blog> blogReference;
    @Service public static Blog blogService;

By default, only classes in the ````controllers```` package are injection candidates.
You can set the ````qi4j.controllers-packages```` parameter to change this behaviour with a
column separated list of package names.

__Limitations__: @Tagged services are not supported yet.

### Qi4j Controllers

Play 2.1 bring the possibility to use non-static Java controllers. This plugin allow
you to manage your controllers inside your Qi4j application.

This is available using ````PlayQiSingle```` and ````PlayQi````, see above.

**Object Controllers**

Let's start with a simple controller ;

    public class MyController extends Controller {

      @Service Blog blog;

      public Result index() {
        return ok( index.render( blog.homepage() ) );
      }

    }

assemble it in your Qi4j application as an Object;

    moduleAssembly.objects( MyController.class );

and then use the PlayQi API in your GlobalSettings ;

    public class Global extends GlobalSettings {

      @Override
      public <T> T getControllerInstance( Class<T> clazz ) {
        return PlayQi.newControllerInstance( clazz );
      }

    }


**Transient Composite Controllers**

Here is a sample Controller as a TransientComposite (meaning you get Qi4j fragments support):

    public interface MyController {

      @MyConcern
      @MySideEffect
      Result index();

    }
    public class MyControllerMixin {

      @Service Blog blog;

      @Override
      public Result index() {
        return ok( index.render( blog.homepage() );
      }
    }

assemble it in your Qi4j application as an Object;

    moduleAssembly.transients( MyController.class ).withMixins( MyControllerMixin.class );

and then use the PlayQi API in your GlobalSettings ;

    public class Global extends GlobalSettings {

      @Override
      public <T> T getControllerInstance( Class<T> clazz ) {
        return PlayQi.newTransientControllerInstance( clazz );
      }

    }

### Compose Play Actions with Qi4j UnitOfWorks

This plugin provides the annotations and to wrap Qi4j UnitsOfWork around Play Actions and Callable<?> wrappers for you to go async.

A Qi4j UnitOfWork takes place in a Module, so depending on your Application assembly you may use different Action composition annotations or Callable<?> wrappers.

**When using SingletonAssembler**

    import org.codeartisans.playqi.*;
    public class MyController {

      @PlayQiSingleUnitOfWorkConcern
      public static Result action() {
        :
        return ok( .. );
      }

    }

If you go async, you can use the ````PlayQiSingleUnitOfWorkCallable```` to wrap a UoW around another Callable.

**When using a full Qi4j Application assembly**

    import org.codeartisans.playqi.*;
    public class MyController {

      // UoW taking place in the configured controllers module
      @ControllersModuleUnitOfWorkConcern
      public static Result action() {
        :
        return ok( .. );
      }

      // UoW taking place in a module of your choice
      @PlayQiUnitOfWorkConcern( "Presentation", "Contexts" )
      public static Result anotherAction() {
        :
        return ok( .. );
      }
    }

If you go async, you can use either the ````ControllersModuleUnitOfWorkCallable```` or the ````PlayQiUnitOfWorkCallable```` to wrap a UoW around another Callable.


### Qi4j Development Tools

Theses tools work in DEV mode only. Their configuration is ignored in TEST and PROD modes.

#### Envisage

By setting ````qi4j.envisage=enabled```` in your ````application.conf````
the [Envisage Qi4j Tool](http://qi4j.org/latest/tools-envisage.html)
is started/reloaded/stopped alongside your application.

![](http://qi4j.org/latest/images/tools-envisage-type.png "Type View")

![](http://qi4j.org/latest/images/tools-envisage-stacked.png "Stacked View")

Envisage is a Swing based visualization tool for the Qi4j Application model,
it allows you to browse your Application Assembly. Visualizations can be printed to PDFs.


#### EntityViewer

By setting ````qi4j.entity-viewer=enabled```` in your ````application.conf````
the [Entity Viewer Qi4j Tool](http://qi4j.org/latest/tools-entity-viewer.html)
is started/reloaded/stopped alongside your application.

![](http://qi4j.org/latest/images/tools-entity-viewer.png "EntityViewer")

EntityViewer is a Swing based Entities browser. It allows you to browse Entities
persisted in EntityStores.

Note that to use the EntityViewer your Qi4j Application Assembly must contains both
EntityStore and Index/Query services.


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

