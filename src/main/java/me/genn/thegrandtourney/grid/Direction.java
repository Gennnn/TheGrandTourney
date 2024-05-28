package me.genn.thegrandtourney.grid;

public enum Direction {
    N,S,E,W;

    public static Direction getDirection(String str) {
     if (str.equalsIgnoreCase("S") || str.equalsIgnoreCase("SOUTH")) {
            return Direction.S;
     } else if (str.equalsIgnoreCase("N") || str.equalsIgnoreCase("NORTH")) {
            return Direction.N;
     } else if (str.equalsIgnoreCase("E") || str.equalsIgnoreCase("EAST")) {
            return Direction.E;
     } else if (str.equalsIgnoreCase("W") || str.equalsIgnoreCase("WEST")) {
             return Direction.W;
     }
      return null;
}

    public static Direction getInverseDirection(Direction direction) {
        if (direction == N) {
            return S;
        } else if (direction == S) {
            return N;
        } else if (direction == E) {
            return W;
        } else if (direction == W) {
            return E;
        } else {
            return null;
        }
    }
}
