package checkers.client.main.model;

import checkers.client.main.util.Pair;

public class OtherMove extends State {
    @Override
    public State select(Pair p) {
        return this;
    }
}
