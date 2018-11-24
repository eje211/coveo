# What's done

## Platform

The easiest platform would have been Tornado. Tornado would have worked perfectly and would have taken a tenth of the
time to develop. However, after telling the team that, if it were up to me, I'd use Scala more often and that I only
resort to Python when I need to take to take other people's skill into account, that felt rather hypocritical. The
easiest framework for me to use in Scala would have been Lift, which has excellent REST support, but Lift doesn't scale
very well. Next is Play. Play would work but is really meant for websites, not for services. The Akka product for
services is Akka HTTP. I thought that Akka HTTP could not be very complicated, compared to Play, so I picked that. And
I was right. It is not very complicated. But it was still some extra software to learn. Still, to its credit, the Akka
documentation is very good.

## Database

There were several reasons why PostgreSQL was my database of choice here. If better geographical accuracy were
necessary, PostGIS would be one, but even before that, its native use of geometric types is already a reason, its
ability to calculate string distances internally is another and its powerful table views are yet another. One of the
reasons why I used table views is that they allow me to keep the give TSV exactly as-is. In case the data needs to be
updated (this is not going to happen here, but it could very well happen in real life), no new could would be necessary.
The format of the data is standardized. That means that the format of the TSV file is standardized and that the new file
will fit the existing table. The replacing the data in the existing table will also update its corresponding view. I
trust that the caching will make that conversion efficient: none of that data ever changes and Postgres is known for its
efficiency.

## ORM

I made the most mistakes in my ORM selection. I first selected Squeryl that plain did not work. Then, I tried
sacalikejdbc, that I did get to work but that posed a huge problem. It is synchronous. That means I could not call it
form an actor. One of the rules of an actor is that it cannot communicate with the rest of the application except
through messages or by calling pure functions. Making a synchronous query to a database from inside an actor cannot be
an acceptable practice.

That's when I switched to Slick. I really should have thought of that in the first place. Slick is not very
difficult to use but its documentation is, in my opinion, terrible. It's one of these cases, far too common in
software were the documentation seems to be made for people who already know the software perfectly to confirm what
they already know. Once I'd figured something out, I could verify that it was indeed in the documentation, but I was
never able to find it there until I already knew what it was. There was a lot of digging through the source code and a
lot of searching through StackOverflow. Again, this is not a criticism of Slick, which is, as far as I can tell,
fantastic, just of the way it's documented.

Slick, rather than returning data structures, returns futures, which can then be consumed asynchronously by Akka actors.
That means that they can be safely called by actors, solving the problem posed by other ORMs.

## Score

For the score, the main goal was to remember that the end consumer of the score would be people, not machines.
I remember being at a conference, Indiecade, and hearing about how mathematical accuracy does not always work wih
humans. The example given was for an action role-playing game that shows the likelihood that a target be hit by a
player. If a player sees that they have a 95% chance of hitting their target and they miss, they think that there's
something wrong with the game or that they have been cheated. If that happens twice in a row, they think that there's
_definitely_ something wrong with the game or that they have _definitely_ been cheated. In fact, both of these
scenarios are absolutely possible. The first one has a chance in twenty of happening. If the player shoots ten times per
minute, it can happen every two minutes. If they shoot more, it can happen, on average, more often than that. It can
happen twice in a row one out of four hundred times. Unlikely, but definitely possible.

Humans don't really accept numbers the way math presents them. Another example of this is the 2016 United States
presidential election. Polls said that there was a 15% chance that the Republican candidate would win. Many took that
to mean that the Republican candidate could not win. But it just meant that there was a 15% chance that he would. And he
did. For the 95% hit chance in the action RPG, the solution advised was to display 80% or 75% hit chance to the player.
They will feel lucky that they win most of the time and accept that the one in twenty times that they don't fall in the
20% or 25% chance that they had of not winning. It is _mathematically_ false, but it _feels_ right to humans, and that's
what matters when humans are the final consumers of the number that is displayed.

So how to present a number that feels right to humans? Another thing that feels right to humans is logarithms. We
humans love logarithms, even those of us who don't know math and don't know what they are. If an object is very close
to me, almost touching me, and another object is a meter further away from me, that meter's worth of difference is
_significant_. However, if the first object is a kilometer away from me, that meter's worth of difference is no longer
significant. If I put that distance on a logarithmic scale, that meter has in fact vanished. A logarithm has put that
subjective assessment is mathematical terms. Sound is a very good example of that. If sound amplitude is multiplied by
ten, it is only increased by one decibel. If that weren't the case, we would only be able to hear a very small range of
sound amplitudes.

This solves another problem: there are two criteria for scoring, distance and name proximity. Both of them yield very
different numbers. Logarithms can bring them both to the same range of numbers.

Now, I am not a mathematician, and I had to experiment a lot to find a formula that was somewhat satisfactory. As for
string distance, I stuck with Levenshtein for two reasons. First of all, it's built into PgostgreSQL. Second, it's well-
known and established. It may not be the best or the most suited for this use case, but finding the absolute best
algorithm would take extensive user testing and is beyond the scope of the exercise.

Taking the maximum value + 1 as the base of the logarithm has the advantage of clamping all the results between 0 and 1.
However, it comes with drawbacks. Mostly, it's not good that the first result be exactly 1 and that the last result be
exactly 0. It would be that the first result is 1 even if it's not "perfect" and that the last result is nothing like
what we looked for, even if it is not completely different. Again, that feels wrong. Also, what if there is only one
result? Then, we'd want it to be closer to 1 than to zero, again, mostly because that _feels_ good. To that end, I found
some fudged parameters that seem to work. Again, I am not a mathematician but they did provide scores that felt good
enough for the inputs I tested with.

Finally, at one point, I attempted a geometric mean, the square root of the product of both scores. But the results at
top just felt a bit too high. Just leaving the product of both scores seemed to give a score that felt better.

## Dependency injection

It might not have been completely necessary, but I really wanted to try using Guice from scratch. I'd never done that.
It really helped me get a better understanding of it. I'd using Guice through other frameworks that used it, but most
of the setting up of Guice had been done for me. Not this time. Akka-HTTP does not require Guice at all.

The use of Guice is not completely absurd here: one might want to implement the configuration interface in a different
way, depending on the use case. In fact, this is what the unit test suite does. (Even though in this specific case,
it would also have worked without the dependency injection.)

## Logging

Originally, there was no logging, but as soon as the service was running on the server, it became clear that having
it run without even a basic logging system was not acceptable, so I configured a basic log4j and started logging a
few things. sbt-native-packager (see below) creates the directories for the log files anyway, so I might as well use
them.

# WHat remains to be done

## Configuration

The configuration system is crude at best. The thought behind that is that in real life, there would most likely be a
centralized configuration system for all or most services. It does not make sense that each service be configured in a
different way. So this service does not go far in setting up its own configuration system.

# Deployment

## Binaries

Given the nature of Akka-HTTP, there are many ways of deploying this service. The one I chose is to use the
sbt-native-packager. Since AWS is Red Hat-based, building to RPM made sense. If I had a better idea of the overall
structure of the system, the Docker plugin might make more sense.) The RPM build command is the following:

    sbt rpm:packageBin

It requires the RNM packages to be installed on the system. They're available on any Unix system, including on
macOS through Homebrew, which is what I used.

As mentioned before, configuration is very crude. The RPM will create a user for this service but not user
directory. That means that it will be *necessary* to create a system configuration file. The address currently
set in the source is:

    /etc/coveolocationservice.conf

But it's trivial to add another: just add a new string to the list. Another way of configuring the package, of
course, is to modify the configuration file in the resource folder, but that runs the danger of putting passwords
in version control, which is never good.

## Database

The database content is included in version control. It should be easy to install on any recent version of
PostgreSQL. The version currently deployed uses RDS. One of way of deploying the data would be:

    psql databasename < sqlcontent.sql

The extensions that this data uses are included by default within Postgres. Any warning or error about them
can be safely ignored.

## API

There is an automatically generated API. It's included in the source. I'm sure it's not finished. I wish I
could have done more with it. Uncommented code always comes back to bite you in the end. But it got so
fascinated in learning Akka-HTTP, Slack and Guice from scratch that it would not have been reasonable to
spend even more time polishing comments for each function.