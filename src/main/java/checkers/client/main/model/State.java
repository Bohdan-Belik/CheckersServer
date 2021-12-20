package checkers.client.main.model;

import checkers.client.main.util.Pair;

public abstract class State {
    public abstract State select(Pair p);
}
