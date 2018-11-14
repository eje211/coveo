

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

