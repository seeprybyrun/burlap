package burlap.behavior.stochasticgame.agents.twoplayer.singlestage.equilibriumplayer;

import java.util.List;
import java.util.Map;
import java.util.Random;

import burlap.behavior.stochasticgame.agents.twoplayer.singlestage.equilibriumplayer.equilibriumsolvers.MaxMax;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.JointActionModel;
import burlap.oomdp.stochasticgames.JointReward;
import burlap.oomdp.stochasticgames.SingleAction;


/**
 * This agent plays an equilibrium solution for two player games based on the immediate joint rewards received for the given state, as if
 * it is a single stage game.
 * By default, the solution concept used will be {@link MaxMax} - assuming the other agent will choose actions that maximize your
 * reward. Different solution concepts can be used by providing a different {@link BimatrixEquilibriumSolver} object in the constructor.
 * @author James MacGlashan
 *
 */
public class EquilibriumPlayingAgent extends Agent {

	/**
	 * The solution concept to be solved for the immediate rewards.
	 */
	protected BimatrixEquilibriumSolver solver;
	
	/**
	 * Random generator for selecting actions according to the solved solution
	 */
	protected Random rand = RandomFactory.getMapped(0);
	
	
	/**
	 * Initializes with the {@link MaxMax} solution concept.
	 */
	public EquilibriumPlayingAgent(){
		this.solver = new MaxMax();
	}
	
	/**
	 * Initializes with strategies formed usign the solution concept generated by the given solver.
	 * @param solver the solver to use for a given solution concept.
	 */
	public EquilibriumPlayingAgent(BimatrixEquilibriumSolver solver){
		this.solver = solver;
	}
	
	@Override
	public void gameStarting() {
		//do nothing
	}

	@Override
	public GroundedSingleAction getAction(State s) {

		List<GroundedSingleAction> myActions = SingleAction.getAllPossibleGroundedSingleActions(s, this.worldAgentName, this.agentType.actions);
		BimatrixTuple bimatrix = this.constructBimatrix(s, myActions);
		solver.solve(bimatrix.rowPayoffs, bimatrix.colPayoffs);
		double [] strategy = solver.getLastComputedRowStrategy();
		GroundedSingleAction selection = myActions.get(this.sampleStrategy(strategy));
		
		return selection;
	}

	@Override
	public void observeOutcome(State s, JointAction jointAction,
			Map<String, Double> jointReward, State sprime, boolean isTerminal) {
		//do nothing
	}

	@Override
	public void gameTerminated() {
		//do nothing
	}
	
	
	/**
	 * Constructs a bimatrix game from the possible joint rewards of the given state. The other agent and their
	 * action set is determined by retreiving the corresponding agent object from the world. Similarly for the joint action model.
	 * If this agent has an internal reward function, they use that; otherwise the world reward function is used.
	 * @param s the state from which the joint rewards are based
	 * @param myActions the set of {@link GroundedSingleAction}s the agent can taken in s.
	 * @return a {@link BimatrixTuple} for the joint reward function.
	 */
	protected BimatrixTuple constructBimatrix(State s, List<GroundedSingleAction> myActions){
		
		JointReward jr = this.world.getRewardModel();
		if(this.internalRewardFunction != null){
			jr = this.internalRewardFunction;
		}
		
		JointActionModel jam = this.world.getActionModel();
		
		
		Agent opponent = this.getOpponent();
		List<GroundedSingleAction> opponentActions = SingleAction.getAllPossibleGroundedSingleActions(s, opponent.getAgentName(), opponent.getAgentType().actions);
		
		BimatrixTuple bimatrix = new BimatrixTuple(myActions.size(), opponentActions.size());
		for(int i = 0; i < bimatrix.nRows(); i++){
			GroundedSingleAction ma = myActions.get(i);
			for(int j = 0; j < bimatrix.nCols(); j++){
				GroundedSingleAction oa = opponentActions.get(j);
				JointAction ja = new JointAction();
				ja.addAction(ma);
				ja.addAction(oa);
				State sp = jam.performJointAction(s, ja);
				Map<String, Double> r = jr.reward(s, ja, sp);
				bimatrix.setPayoff(i, j, r.get(this.worldAgentName), r.get(opponent.getAgentName()));
				
			}
		}
		
		return bimatrix;
	}
	
	
	/**
	 * Samples an action from a strategy, where a strategy is defined as probability distribution over actions.
	 * @param strategy a double array where strategy[i] is the probability of action i being selected
	 * @return a sampled action
	 */
	protected int sampleStrategy(double [] strategy){
		double roll = this.rand.nextDouble();
		double sum = 0.;
		for(int i = 0; i < strategy.length; i++){
			sum += strategy[i];
			if(roll < sum){
				return i;
			}
		}
		throw new RuntimeException("Strategy probability distribution does not sum to 1; it sums to: " + sum);
	}
	
	
	/**
	 * Returns the {@link Agent} object in the world for the opponent.
	 * @return the {@link Agent} object in the world for the opponent.
	 */
	protected Agent getOpponent(){
		List<Agent> agents = this.world.getRegisteredAgents();
		if(agents.size() != 2){
			throw new RuntimeException("EquilibriumPlayingAgent is only defined for two player games and there are " + agents.size() + " players in the world.");
		}
		if(agents.get(0) == this){
			return agents.get(1);
		}
		return agents.get(0);
	}
	
	
	/**
	 * A Bimatrix tuple. It consists of a 2D double array for the row player's payoffs and a 2D douuble array for the column player's payoffs.
	 * @author James MacGlashan
	 *
	 */
	protected class BimatrixTuple{
		
		/**
		 * The row player's payoffs.
		 */
		public double [][] rowPayoffs;
		
		/**
		 * The column player's payoffs.
		 */
		public double [][] colPayoffs;
		
		
		/**
		 * Initializes the payoff matrices for a bimatrix of the given row and column dimensionality
		 * @param nRows the number of rows
		 * @param nCols the number of columns
		 */
		public BimatrixTuple(int nRows, int nCols){
			this.rowPayoffs = new double[nRows][nCols];
			this.colPayoffs = new double[nRows][nCols];
		}
		
		
		/**
		 * Initializes with a given row and column player payoffs.
		 * @param rowPayoffs the row player payoffs.
		 * @param colPayoffs the column player payoffs.
		 */
		public BimatrixTuple(double [][] rowPayoffs, double [][] colPayoffs){
			if(rowPayoffs.length != colPayoffs.length || rowPayoffs[0].length != colPayoffs[0].length){
				throw new RuntimeException("Payoff matrices are not of equal dimension.");
			}
			this.rowPayoffs = rowPayoffs;
			this.colPayoffs = colPayoffs;
		}
		
		/**
		 * Returns the number of rows
		 * @return the number of rows
		 */
		public int nRows(){
			return rowPayoffs.length;
		}
		
		
		/**
		 * Returns the number of columns
		 * @return the number of columns
		 */
		public int nCols(){
			return rowPayoffs[0].length;
		}
		
		
		/**
		 * Sets the payoffs for a given row and column.
		 * @param row the row index (player 1's action).
		 * @param col the column index (player'2 action).
		 * @param rowPayoff the payoff the row player receives.
		 * @param colPayoff the payoff the column player receives.
		 */
		public void setPayoff(int row, int col, double rowPayoff, double colPayoff){
			this.rowPayoffs[row][col] = rowPayoff;
			this.colPayoffs[row][col] = colPayoff;
		}
	}
	

	

}
