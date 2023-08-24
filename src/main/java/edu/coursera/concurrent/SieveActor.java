package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;

import java.util.ArrayList;
import java.util.List;

import static edu.rice.pcdp.PCDP.finish;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 *
 * countPrimes to determin the number of primes <= limit.
 */
public final class SieveActor extends Sieve {

    /**
     * {@inheritDoc}
     *
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {
        final SieveActorActor actor = new SieveActorActor(2);
        finish(() -> {
            for (int i = 3; i <= limit; i += 2) {
                actor.send(i);
            }
        });

        int n = 0;
        SieveActorActor loopActor = actor;
        while (loopActor != null) {
            n += loopActor.primes.size();
            loopActor = loopActor.nextActor;
        }

        return n;

    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {

        private static final int MAX_PRIMES = 1_000;

        private List<Integer> primes = new ArrayList<>();
        private SieveActorActor nextActor;

        SieveActorActor(final int primesCount) {
            this.primes.add(primesCount);
        }

        /**
         * Process a single message sent to this actor.
         *
         *
         * @param msg Received message
         */
        @Override
        public void process(final Object msg) {

            final int candidate = (Integer) msg;

            final boolean isLocally =  primes.stream().allMatch( s -> candidate % s != 0);

            if (isLocally) {
                if (primes.size() < MAX_PRIMES) {
                    primes.add(candidate);
                } else if (nextActor != null) {
                    nextActor.send(candidate);
                } else {
                    nextActor = new SieveActorActor(candidate);
                }
            }

        }


    }
}
