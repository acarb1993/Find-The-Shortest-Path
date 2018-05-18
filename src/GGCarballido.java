import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

/* Adam Carballido
 *  A Java program that will make simple graphs and calculate shortest paths between verticies.
 */

class GGCarballido {
	public static void main(String[] args) {
		MainFrame frame = new MainFrame();
	}
} // End GGCarballido

// Main Frame where the panels will reside.
class MainFrame extends JFrame {
	private final static int WIDTH = 1200;
	private final static int HEIGHT = 900;

	public MainFrame() {
		setTitle("Graph-Maker");
		setSize(WIDTH, HEIGHT);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new GridLayout(1, 1) );

		GraphPanel graphPanel = new GraphPanel(WIDTH / 2, HEIGHT / 2);
		ButtonPanel buttonPanel= new ButtonPanel(WIDTH / 2, HEIGHT / 2, graphPanel); 
		
		graphPanel.addButtonPanel(buttonPanel);
		
		add(buttonPanel);
		add(graphPanel);

		setResizable(false);
		setLocationRelativeTo(null); // Centers the frame on screen
		setVisible(true);
	}

	public int getWidth() { return WIDTH; }

	public int getHeight() { return HEIGHT; }

} // End MainFrame Class

class GraphPanel extends JPanel {
	private int width, height, vertX, vertY, vStoreCap;
	
	private ButtonPanel buttonPanel;
	
	private ArrayList<Vertex> verticies;
	private ArrayList<Vertex> tempVerticies;
	private ArrayList<Edge> tempEdges;
	private ArrayList<Edge> edges;
	private ArrayList<Vertex> vStore;
	LinkedList<Vertex> shortestPath;
	
	private Set<Vertex> settledNodes;
	private Set<Vertex> unSettledNodes;
	
	private HashMap<Vertex, Vertex> predecessors;
	private HashMap<Vertex, Integer> distance;
	
	
	public GraphPanel(int width, int height) {
		width = height = vertX = vertY = 0;
		
		verticies = new ArrayList<Vertex>();
		edges = new ArrayList<Edge>();
		vStore = new ArrayList<Vertex>(2); // Stores the two verticies seleceted to make an edge
		vStoreCap = 2;

		settledNodes = new HashSet<Vertex>();
		unSettledNodes = new HashSet<Vertex>();
		distance = new HashMap<Vertex, Integer>();
		predecessors = new HashMap<Vertex, Vertex>();
		
		setBorder(BorderFactory.createTitledBorder("Graph") );		
		setPreferredSize(new Dimension(width, height) );
		setLayout(null);
	
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (buttonPanel.vertexButtonIsOn() ) {
					drawVertex(e.getX(), e.getY());
					verticies.add(new Vertex(e.getX(), e.getY()));
				}
				
				else if (buttonPanel.edgeButtonIsOn() ) {
					for (int i = 0; i < verticies.size(); i++) {
						if (findClosestVertex(e.getX(), e.getY(), verticies.get(i) ) ) {
							System.out.print("Found a " + verticies.get(i).getClass().getName() + " at " + verticies.get(i).getX() + ", " + verticies.get(i).getY() );
							System.out.println();
							System.out.println("I clicked at: " + e.getX() + ", "+ e.getY() );
							vStore.add(verticies.get(i) );
							if (vStore.size() == vStoreCap) {
								if (vStore.get(0) == vStore.get(1) )  
									vStore.remove(1); 
										
								else { 
								    edges.add(new Edge(vStore.get(0), vStore.get(1) ) );
								    drawEdge(vStore.get(0), vStore.get(1) );
								    vStore.get(0).addToAdjacencyList(vStore.get(1) );
								    vStore.get(1).addToAdjacencyList(vStore.get(0) );
								    vStore.clear();
								}
							}
						}
					}
				} // end else if
				
				else if (buttonPanel.moveVertexButtonIsOn() ) {
					for (int i = 0; i < verticies.size(); i++) {
						if (findClosestVertex(e.getX(), e.getY(), verticies.get(i) ) ) {
							System.out.print("Found a " + verticies.get(i).getClass().getName() + " at " + verticies.get(i).getX() + ", " + verticies.get(i).getY() );
							System.out.println();
							System.out.println("I clicked at: " + e.getX() + ", "+ e.getY() );
							vStore.add(verticies.get(i) );
						}
					}
					vStore.clear();
				}
				
				else if (buttonPanel.shortestPathButtonIsOn() ) {
					for (int i = 0; i < verticies.size(); i++) {
						if(findClosestVertex(e.getX(), e.getY(), verticies.get(i) ) ) {
							System.out.print("Found a " + verticies.get(i).getClass().getName() + " at " + verticies.get(i).getX() + ", " + verticies.get(i).getY() );
							System.out.println();
							System.out.println("I clicked at: " + e.getX() + ", "+ e.getY() );
							vStore.add(verticies.get(i) );
							if(vStore.size() == vStoreCap) {
								if(vStore.get(0) == vStore.get(1) )
									vStore.remove(1);
								
								else { // Dijkstra's Algorithm
									distance.put(vStore.get(0), 0);
									unSettledNodes.add(vStore.get(0));
									tempVerticies = verticies;
									tempEdges = edges;
									
									while(unSettledNodes.size() > 0) {
										Vertex node = getMinimum(unSettledNodes);
										settledNodes.add(node);
										unSettledNodes.remove(node);
										findMinimalDistances(node);
									}
									shortestPath = getPath(vStore.get(1) );
									for (i = 0; i < shortestPath.size(); i++) {
										System.out.println(shortestPath.get(i).getName() );
									
									}
									// Starts everything from scratch after the algo is done.
									vStore.clear();
									shortestPath.clear();
									settledNodes.clear();
									unSettledNodes.clear();
									distance.clear();
									predecessors.clear();
								}
							}
						}
					}
				} // end else if
			} // end mouse pressed
		});

		// TODO move Vertex
		addMouseListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent e) {
		
			}
		});
	} // End Constructor
	
	public LinkedList<Vertex> getShortestPath() { return shortestPath; }
	
	private void findMinimalDistances(Vertex node) {
		ArrayList<Vertex> adjacentNodes = getNeighbors(node);
		for (Vertex t: adjacentNodes) {
			if (getShortestDistance(t) > getShortestDistance(node) 
					+ getDistance(node, t) ) {
				distance.put(t, getShortestDistance(node) 
						+ getDistance(node, t) );
				predecessors.put(t, node);
				unSettledNodes.add(t);
			}
		}
	}
	
	private int getDistance(Vertex node, Vertex target) {
		for (Edge e: tempEdges) {
			if (e.getSource().equals(node)
					&& e.getDestination().equals(target) ) {
				return e.getWeight();
			}
		}
		throw new RuntimeException("Snarf");
	}
	
	private ArrayList<Vertex> getNeighbors(Vertex node) {
		ArrayList<Vertex> neighbors = new ArrayList<Vertex>();
		for(Edge e: tempEdges) {
			if (e.getSource().equals(node) 
					&& !isSettled(e.getDestination() ) ) {
				neighbors.add(e.getDestination() );
			}
		}
		return neighbors;
	}
	
	private Vertex getMinimum(Set<Vertex> tempVerticies) {
		Vertex min = null; 
		for (Vertex v: tempVerticies) {
			if (min == null) { min = v; }
			
			else { if (getShortestDistance(v) < getShortestDistance(min) ) { min = v; } }
		}
		
		return min;
	}
	
	private int getShortestDistance(Vertex destination) {
		Integer d = distance.get(destination);
		if(d == null) { return Integer.MAX_VALUE; }
		
		else { return d; }
	}
	
	private boolean isSettled(Vertex v) { return settledNodes.contains(v); }
	
	public LinkedList<Vertex> getPath(Vertex t) {
		LinkedList<Vertex> path = new LinkedList<Vertex>();
		Vertex step = t;
		
		if(predecessors.get(step) == null) {
			return null;
		}
		
		path.add(step);
		while(predecessors.get(step) != null) {
			step = predecessors.get(step);
			path.add(step);
		}
		Collections.reverse(path);
		return path;
	}
	
	public ArrayList<Vertex> getVerticies() { return verticies; }
	
	public ArrayList<Edge> getEdges() { return edges; }
	
	// Will find the the closest Vertex where the user clicked depending on the offset.
	private boolean findClosestVertex(int x, int y, Vertex v) {
		int offset = 10;
		if ( ( (x - v.getX() >= 0) && (x - v.getX() <= offset)  ||
			(v.getX() - x <= offset) && (v.getX() - x >= 0)  ) &&
			( (y - v.getY() >= 0) && (y - v.getY() <= offset) ||
			(v.getY() - y <= offset) && (v.getY() - y >= 0) ) ||
			 (x == v.getX() ) && (y == v.getY() ) ) 
			return true;
		
		else return false;
	}
	
	private void drawVertex(int x, int y) {
			vertX = x;
			vertY = y;
			repaint();
	}
	
	private void drawEdge(Vertex source, Vertex desination) {
		repaint();
	}
	
	protected void redrawGraph(Graphics g) {
		for(Vertex v: verticies)
			v.paintVertex(g);
		
		for(Edge e: edges) 
			e.paintEdge(g);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (buttonPanel.vertexButtonIsOn() ) 
		    verticies.get(verticies.size() - 1).paintVertex(g);
		
		if(buttonPanel.edgeButtonIsOn() )
			edges.get(edges.size() - 1).paintEdge(g);
		
		redrawGraph(g);
	}
	
	public void addButtonPanel(ButtonPanel panel) { buttonPanel = panel; } // Allows manipulation of button panel in the graph panel
	
} // End GraphPanel Class

// The panel responsible for buttons and functions of the GUI
class ButtonPanel extends JPanel {
	private int width, height;
	private GraphPanel graphPanel;
	private ArrayList<JRadioButton> radioButtons;
	private ArrayList<JButton> jButtons;
	
	JRadioButton addVertex;
	JRadioButton addEdge;
	JRadioButton moveVertex;
	JRadioButton shortestPath;
	JRadioButton changeWeightTo;
	
	JButton addAllEdges;
	JButton randomWeights;
	JButton minSpanningTree;
	JButton help;
	
	ButtonGroup group;

	public ButtonPanel(int width, int height, GraphPanel gp) {
		setBorder(BorderFactory.createTitledBorder("Options") );
		setPreferredSize(new Dimension(width, height) );
		setLayout(new GridBagLayout() );
		
		graphPanel = gp;
		
		radioButtons = new ArrayList<JRadioButton>();
		jButtons = new ArrayList<JButton>();
		group = new ButtonGroup(); // Makes it so only one radio button is selected a time 

		GridBagConstraints gbc = new GridBagConstraints(); // Used to manipulate container placement in GridBag
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, -75, 35, 25); // Space between containers: top, left, bottom, right

		addVertex = new JRadioButton("Add Vertex");
		radioButtons.add(addVertex);
		
		addEdge = new JRadioButton("Add Edge");
		radioButtons.add(addEdge);
		
		moveVertex= new JRadioButton("Move Vertex");
		radioButtons.add(moveVertex);
		
		shortestPath = new JRadioButton("Shortest Path");
		radioButtons.add(shortestPath);
		
		changeWeightTo = new JRadioButton("Change Weight To");
		radioButtons.add(changeWeightTo);
		
		for (int i = 0; i < radioButtons.size(); i++) {
			radioButtons.get(i).setFont(radioButtons.get(i).getFont().deriveFont(24.0f));
			gbc.gridx = 0;
			gbc.gridy = i;
			add(radioButtons.get(i), gbc);
			group.add(radioButtons.get(i));
		}
				
		// TODO will let the user change the weight of a given verticie
		JTextPane textPane = new JTextPane();
		textPane.setPreferredSize(new Dimension(200, 50));
		textPane.setFont(textPane.getFont().deriveFont(30.0f));

		gbc.gridx = 1;
		gbc.gridy = 4;
		add(textPane, gbc);

		addAllEdges = new JButton("Add All Edges");
		jButtons.add(addAllEdges);
		
		randomWeights = new JButton("Random Weights");
		jButtons.add(randomWeights);
		randomWeights.addActionListener(new RandomWeightAction(graphPanel) );
		
		minSpanningTree = new JButton("Minimal Spanning Tree");
		jButtons.add(minSpanningTree);
		
		help = new JButton("Help");
		jButtons.add(help);
		help.addActionListener(new HelpAction() ); // Displays the help window
		
		
		for (int i = 0; i < jButtons.size(); i++) {
			gbc.gridx = 0;
			gbc.gridy = i + radioButtons.size(); // Y position will not collide with the previously placed buttons
		
			add(jButtons.get(i), gbc);
			jButtons.get(i).setFont(jButtons.get(i).getFont().deriveFont(24.0f));
		}
	} 
	
	public void addGraphPanel(GraphPanel panel) { graphPanel = panel; } // Allows manipulation of the graph panel in the button panel
	
	// Methods to check if a particular button is selected.
	public boolean vertexButtonIsOn() { return addVertex.isSelected(); }
	
	public boolean edgeButtonIsOn() { return addEdge.isSelected(); }
	
	public boolean moveVertexButtonIsOn() { return moveVertex.isSelected(); }
	
	public boolean shortestPathButtonIsOn() { return shortestPath.isSelected(); }
	
	public boolean changeWeightToButtonIsOn() { return changeWeightTo.isSelected(); }
	
} // End ButtonPanel Class

class Vertex {
	private int xCord, yCord, vertW, vertH, distanceValue;
	private char name; // The letter name starting with 'a' of the vertex
	private static int numberOfVerticies = 0;
	private ArrayList<Vertex> adjacencyList;
	private boolean isSelected;
	
	public Vertex(int x, int y) {
		xCord = x;
		yCord = y;
		vertW = 20;
		vertH = 20;
		distanceValue = 0;
		numberOfVerticies++;
		name = (char) ('a' + numberOfVerticies - 1);
		adjacencyList = new ArrayList<Vertex>();
		isSelected = false;
	}
	
	// Returns the value of the x Coordinate
	public int getX() { return xCord; }
	
	// Returns the value of the y Coordinate
	public int getY() { return yCord; }
	
	public int getDistanceValue() { return distanceValue; }
	
	public void setDistanceValue(int d) { distanceValue = d; }
	
	public char getName() { return name; }
	
	public ArrayList<Vertex> getAdjacencyList() { return adjacencyList; }
	
	public void addToAdjacencyList(Vertex v) { adjacencyList.add(v); }
	
	public void moveVertex(int newX, int newY) {
		xCord = newX;
		yCord = newY;
	}
	
	public void toggleSelected() { 
		if (isSelected) isSelected = false;
		
		else isSelected = true;
	}
	
	@Override
	public String toString() { return xCord + ", " + yCord; }
	
	public void paintVertex(Graphics g) {
		int x, y;
		x = xCord - (vertW / 2);
		y = yCord - (vertH / 2);
		
		g.setColor(Color.RED);
		g.fillOval(x, y, vertW, vertH);
		g.setColor(Color.BLACK);
		g.drawOval(x, y, vertW, vertH);
		g.setFont(new Font("Sans Serif", Font.PLAIN, 20) );
		g.drawString(Character.toString(name), x, y);
	}

} // End Vertex

class Edge {
	private int xCord, yCord, midpointX, midpointY, weight;
	private Vertex source, destination; // Source is starting vertex, destination is ending vertex
	
	public Edge(Vertex s, Vertex d) {
		source = s;
		destination = d;
		midpointX = (source.getX() + destination.getX() ) / 2;
		midpointY = (source.getY() + destination.getY() ) / 2;
		weight = 0;
	}
	
	public int getX() { return xCord; }
	
	public int getY() { return yCord; }
	
	public int getWeight() { return weight; }
	
	public void setWeight(int w) { weight = w; }
	
	public void paintEdge(Graphics g) {
		g.setColor(Color.BLUE);
		g.drawLine(source.getX(), source.getY(), destination.getX(), destination.getY());
		g.setFont(new Font("Sans Serif", Font.PLAIN, 20) );
		g.drawString(Integer.toString(weight), midpointX, midpointY);
	}
	
	public Vertex getSource() { return source; }
	
	public Vertex getDestination() { return destination; }
	
} // End Edge

class RandomWeightAction implements ActionListener {
	private GraphPanel graphPanel;
	private Random rand;
	
	public RandomWeightAction(GraphPanel gp) {
		graphPanel = gp;
		rand = new Random();
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		for (int i = 0; i < graphPanel.getEdges().size(); i++) {
			int n = rand.nextInt(20) + 1;
			( (Edge) graphPanel.getEdges().get(i)).setWeight(n);
			graphPanel.repaint();
		}
	}
} // End RandomWeightAction

class VertexComparator implements Comparator<Vertex> {

	@Override
	public int compare(Vertex v1, Vertex v2) {
		if (v1.getDistanceValue() - v2.getDistanceValue() > 0) 
			return 1;
		
		else if (v1.getDistanceValue() - v2.getDistanceValue() < 0)
			return -1;
		
		else return 0;
	}
} // End VertexComparator

// Action for the help button
class HelpAction implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		JFrame frame = new JFrame("Help Window");
		frame.setSize(new Dimension(1200, 800));
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setResizable(false);

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout() );
		GridBagConstraints gbc = new GridBagConstraints(); 
		gbc.insets = new Insets(15, 10, 15, 0); // Space between containers: top, left, bottom, right

		// Creates the labels and sets their constraints of the grid bag layout
		JLabel[] labels = new JLabel[9];
		for (int i = 0; i < labels.length; i++) {
			labels[i] = new JLabel("", SwingConstants.LEFT);
			labels[i].setFont(new Font("Century", Font.PLAIN, 30) );
			gbc.gridx = 0;
			gbc.gridy = i;
			panel.add(labels[i], gbc);
		}

		labels[0].setText("How to Use: ");
		labels[0].setFont(new Font("Century", Font.PLAIN, 50) );
		labels[1].setText("Add Vertex: Click on an area on the graph panel to place a vertex. ");
		labels[2].setText("Add Edge: Click on two verticies to connect them with an edge.");
		labels[3].setText("Move Vertex: Moves a vertex to another area on the graph panel.");
		labels[4].setText("Shortest Path: Will calculate the shortest path from one vertice to the other.");
		labels[5].setText("Change Weight To: Click on an edge and enter the weight to be changed in the text field.");
		labels[6].setText("Add All Edges: Adds all possible edges between two verticies. ");
		labels[7].setText("Random Weights: Gives each edge a random weight.");
		labels[8].setText("Minimal Spanning Tree: Finds a path between all verticies with the lowest cost.");

		frame.add(panel);

		frame.setVisible(true);
	}
} // End HelpAction Class

