# DecisionTree
This is a decision tree framework for evaluating game-playing strategies

## Introduction

This framework is related to both minimax and Monte Carlo Tree Search techniques.
The basic idea is that we create a tree of (self)-expanding nodes.
As each new node is created, we check its state (the underlying value of the node).
If the goal has been reached, or if we determine that the goal can never be reached,
then we return that unwind the recursion with the tree in its current state.

Alternately, we continue by determining the successors of the current node
and expanding each in turn.

There are two other ways to terminate the expansion:
(1) the maximum number of moves is reached;
(2) or a somewhat arbitrary runaway condition is reached.
