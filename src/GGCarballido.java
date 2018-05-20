import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Random;


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
	private int width, height;
	
	private ButtonPanel buttonPanel;
	
	Vertex u, v; // Storing the verticies clicked by user
	
	private ArrayList<Vertex> verticies; // Positional verticie list
	private ArrayList<Edge> edges; // Positional edge list
	private HashMap<Position, Vertex> vMap; // Stores the locations of Verticies and their positions
	private HashMap<Vertex, Edge> adjacencyMap; 
	private HashMap<Vertex, Integer> distances; // Map of the distances between all verticies in shortest path
	private HashMap<Vertex, Vertex> predecessors; // Map of verticies that are adjacent to each other
	private PriorityQueue<Vertex> pq;
	
	public GraphPanel(int width, int height) {
		width = height = 0;
		
		verticies = new ArrayList<Vertex>();
		edges = new ArrayList<Edge>();
	
		adjacencyMap = new HashMap<Vertex, Edge>(); 
		distances = new HashMap<Vertex, Integer>();
		predecessors = new HashMap<Vertex, Vertex>();
		vMap = new HashMap<Position, Vertex>();
		
		setBorder(BorderFactory.createTitledBorder("Graph") );		
		setPreferredSize(new Dimension(width, height) );
		setLayout(null);
	
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (buttonPanel.vertexButtonIsOn() ) {
					insertVertex(new Position(e.getX(), e.getY() ) );
				}
				
				else if (buttonPanel.edgeButtonIsOn() ) {
					if (findClosestVertex(e.getX(), e.getY() ) ) {	
						if (u != null && v != null) {
							turnOffVerticies();
							edges.add(insertEdge(u, v));
							adjacencyMap.put(u, getLastEdgeAdded() );
							adjacencyMap.put(v, getLastEdgeAdded() );
							u = v = null;
						}
					}
				}
				
				else if (buttonPanel.moveVertexButtonIsOn() ) {
					if (u == null) {
						findClosestVertex(e.getX(), e.getY() );
					}
					
					else {
						u.moveVertex(e.getX(), e.getY());
						u.turnOff();
						repaint();
						u = v = null;
					}
				}
				
				else if (buttonPanel.shortestPathButtonIsOn() ) {
						if (findClosestVertex(e.getX(), e.getY() )) {
							if (u != null && v != null) {
								turnOffVerticies();
								shortestPath(u);
								shortestPath(u, v);
								distances.clear();
								predecessors.clear();
								u = v = null;
							}
						}
					}
				
				else if (buttonPanel.changeWeightToButtonIsOn()) {
					if (findClosestVertex(e.getX(), e.getY() ) ) {	
						if (u != null && v != null) {
							if (hasEdge(u, v)) {
								turnOffVerticies();
								
								if (buttonPanel.getChangeWeightTextPanel().getText().trim().isEmpty() ) {
									turnOffVerticies();
									u = v = null;
								}
					
								else {
									String textField = buttonPanel.getChangeWeightTextPanel().getText(); 
									int newWeight = Integer.parseInt(textField);
									getEdge(u, v).setWeight(newWeight);
									u = v = null;
									}
							}
							else {
								turnOffVerticies();
								u = v = null;
							}
						}
					}
				}
			} // End mousePressed
		}); // End Mouse Adapter
	} // End constructor
	
	public Vertex getU() { return u; }
	
	public Vertex getV() { return v; }
	
	// Finds the shortest path between two verticies
	public ArrayList<Vertex> shortestPath(Vertex source, Vertex target) {
		turnOffEdges();
		ArrayList<Vertex> path = new ArrayList<Vertex>();
		Vertex step = target;
		
		path.add(step);
		while(predecessors.get(step) != null) {
			step = predecessors.get(step);
			path.add(step);
			
		}
		
		Collections.reverse(path);	
		
		for (int i = 0; i < path.size() - 1; i++) 
			getEdge(path.get(i), path.get(i + 1)).toggleSelected();
		
		repaint();
		
		return path;
	}
	
	// Gets the distance values of the verticies. The source is the first selected Vertex
	public void shortestPath(Vertex source) {
		pq = new PriorityQueue<Vertex>();
		distances.put(source, 0);
		for (Vertex v : verticies) {
			if (v != source)
				distances.put(v, 99);
			pq.add(v);
		}
		
		while (!pq.isEmpty()) {
			Vertex u = pq.remove();
			for (Vertex v : u.getAdjacencyMap().keySet() ) {
				if (distances.get(v) > distances.get(u) + getEdge(u, v).getWeight() ) {
					distances.replace(v, distances.get(u) + getEdge(u, v).getWeight());
					pq.add(v);
					predecessors.put(v, u);
				}
			}
		}
	}
	
	public boolean hasEdge(Vertex u, Vertex v) {
		Vertex origin = u;
		return origin.getAdjacencyMap().get(v) != null;
	}
	
	// Returns the edge from u to v, or null if they are not adjacent;
	public Edge getEdge(Vertex u, Vertex v) {
		Vertex origin = u;
		return origin.getAdjacencyMap().get(v);
	}
	
	// Returns the verticies of edge e as an array of length two
	public Vertex[] endVerticies(Edge e){
		return e.getEndPoints();
	}
	
	// Returns the vertex that is opposite vertex v on edge e
	public Vertex opposite(Vertex v, Edge e) throws IllegalArgumentException {
		Edge edge = e;
		Vertex[] endpoints = edge.getEndPoints();
		if (endpoints[0] == v)
			return endpoints[1];
		else if (endpoints[1] == v)
			return endpoints[0];
		else {
			throw new IllegalArgumentException("v is not incident to this edge");
		}
	}
	
	public Vertex insertVertex(Position p) {
		drawVertex();
		Vertex vert = new Vertex(p);
		verticies.add(vert);
		vMap.put(p, vert);
		return vert;
	}
	
	public Edge insertEdge(Vertex u, Vertex v) throws IllegalArgumentException {
		if (getEdge(u, v) == null) {
			Edge e = new Edge(u, v);
			drawEdge(u, v);
			Vertex origin = u;
			Vertex destination = v;
			origin.getAdjacencyMap().put(v, e);
			destination.getAdjacencyMap().put(u, e);
			edges.add(e);
			return e;
		}
		else {
			u = v = null;
			throw new IllegalArgumentException("Edge from u to v exists");
			
		}
	}
	
	private void turnOffVerticies() {
		for (Vertex v : verticies) 
			v.turnOff();
	}
	
	private void turnOffEdges() {
		for (Edge e : edges) 
			e.turnOff();
	}
	
	public Edge getLastEdgeAdded() { return edges.get(edges.size() - 1); }
	
	public ArrayList<Vertex> getVerticies() { return verticies; }
	
	public ArrayList<Edge> getEdges() { return edges; }
	
	// Will find the the closest Vertex where the user clicked depending on the offset.
	private boolean findClosestVertex(int x, int y) {
		int offset = 10;
		
		for (int i = 0; i < verticies.size(); i++) {
			if ( ( (x - verticies.get(i).getX() >= 0) && (x - verticies.get(i).getX() <= offset)  ||
					(verticies.get(i).getX() - x <= offset) && (verticies.get(i).getX() - x >= 0)  ) &&
					( (y - verticies.get(i).getY() >= 0) && (y - verticies.get(i).getY() <= offset) ||
							(verticies.get(i).getY() - y <= offset) && (verticies.get(i).getY() - y >= 0) ) ||
					(x == verticies.get(i).getX() ) && (y == verticies.get(i).getY() ) ) {
				if (u == null) {
					u = verticies.get(i);
					u.turnOn();
					repaint();
					return true;
				}
				
				else if (u != null) {
					v = verticies.get(i);
					v.turnOn();
					repaint();
					return true;
				}
			}
		
		}
		return false;
	}
	
	private void drawVertex() {
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
		
		if(buttonPanel.edgeButtonIsOn() && edges.size() > 0)
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
	
	JTextPane textPane;

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
				
		textPane = new JTextPane();
		textPane.setPreferredSize(new Dimension(200, 50));
		textPane.setFont(textPane.getFont().deriveFont(30.0f));

		gbc.gridx = 1;
		gbc.gridy = 4;
		add(textPane, gbc);

		addAllEdges = new JButton("Add All Edges");
		jButtons.add(addAllEdges);
		addAllEdges.addActionListener(new AddAllEdgesAction(graphPanel, graphPanel.getVerticies() ) );
		
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
	
	// TODO add change weight action
	public boolean changeWeightToButtonIsOn() { return changeWeightTo.isSelected(); }
	
	public JTextPane getChangeWeightTextPanel() { return textPane; }
	
} // End ButtonPanel Class

class Vertex implements Comparable<Vertex> {
	private int vertW, vertH, distanceValue;
	private Position p;
	private char name; // The letter name starting with 'a' of the vertex
	private static int numberOfVerticies = 0;
	private HashMap<Vertex, Edge> adjacencyMap;
	private boolean isSelected;
	
	public Vertex(Position pos) {
		p = pos;
		vertW = 15;
		vertH = 15;
		distanceValue = 0;
		numberOfVerticies++;
		name = (char) ('a' + numberOfVerticies - 1);
		adjacencyMap = new HashMap<Vertex, Edge>();
		isSelected = false;
	}
	
	// Returns the value of the x Coordinate
	public int getX() { return p.getXCord(); }
	
	// Returns the value of the y Coordinate
	public int getY() { return p.getYCord(); }
	
	public Position getPosition() { return p; }
	
	public int getDistanceValue() { return distanceValue; }
	
	public void setDistanceValue(int d) { distanceValue = d; }
	
	public char getName() { return name; }
	
	public HashMap<Vertex, Edge> getAdjacencyMap() { return adjacencyMap; }
	
	public void addToAdjacencyMap(Vertex v, Edge e) { adjacencyMap.put(v, e); }
	
	public void moveVertex(int x, int y) {
		p.setXCord(x);
		p.setYCord(y);
	}
	
	public void turnOn() { isSelected = true; }
	
	public void turnOff() { isSelected = false; }
	
	public void paintVertex(Graphics g) {
		int x, y;
		x = p.getXCord() - (vertW / 2);
		y = p.getYCord() - (vertH / 2);
		
		
		g.setColor(Color.RED);
		g.fillOval(x, y, vertW, vertH);
		
		if (isSelected)
			g.setColor(Color.GREEN);
		
		else 
			g.setColor(Color.BLACK);
		g.drawOval(x, y, vertW, vertH);
		g.setFont(new Font("Sans Serif", Font.PLAIN, 20) );
		g.drawString(Character.toString(name), x, y);
	}

	@Override
	public int compareTo(Vertex v) {
		return distanceValue - v.getDistanceValue();			
	}

} // End Vertex

class Edge {
	private int xCord, yCord, midpointX, midpointY, weight;
	private Vertex[] endpoints; // Source is starting vertex, destination is ending vertex
	private boolean isSelected;
	Random rand;
	
	public Edge(Vertex s, Vertex d) {
		endpoints = (Vertex[]) new Vertex[] {s, d};
		midpointX = (endpoints[0].getX() + endpoints[1].getX() ) / 2;
		midpointY = (endpoints[0].getY() + endpoints[1].getY() ) / 2;
		rand = new Random();
		weight = rand.nextInt(20) + 1;;
		isSelected = false;
	}
	
	public int getX() { return xCord; }
	
	public int getY() { return yCord; }
	
	public int getWeight() { return weight; }
	
	public Vertex[] getEndPoints() { return endpoints; }
	
	public void setWeight(int w) { weight = w; }
	
	public void paintEdge(Graphics g) {
		if(isSelected) 
			g.setColor(Color.GREEN);
		
		else 
			g.setColor(Color.BLUE);
		
		g.drawLine(endpoints[0].getX(), endpoints[0].getY(), endpoints[1].getX(), endpoints[1].getY());
		g.setFont(new Font("Sans Serif", Font.PLAIN, 20) );
		
		midpointX = (endpoints[0].getX() + endpoints[1].getX() ) / 2;
		midpointY = (endpoints[0].getY() + endpoints[1].getY() ) / 2;
		
		g.drawString(Integer.toString(weight), midpointX, midpointY);
	}
	
	public Vertex getSource() { return endpoints[0]; }
	
	public Vertex getDestination() { return endpoints[1]; }
	
	public void toggleSelected() { isSelected = !isSelected; }
	
	public void turnOff() { isSelected = false; }
	
} // End Edge

class Position {
	int xCord, yCord;
	
	public Position(int x, int y) {
		xCord = x;
		yCord = y;
	}
	
	public int getXCord() { return xCord; }
	
	public int getYCord() { return yCord; }
	
	public void setXCord(int x) { xCord = x; }
	
	public void setYCord(int y) { yCord = y; }
}

class AddAllEdgesAction implements ActionListener {
	private GraphPanel g;
	private ArrayList<Vertex> verticies;
	private Vertex u;
	
	public AddAllEdgesAction(GraphPanel gp, ArrayList<Vertex> v) {
		g = gp;
		verticies = v;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		ArrayList<Vertex> unvisited = new ArrayList<Vertex>();
		
		for (Vertex v : verticies)
			unvisited.add(v);
		
		while(unvisited.size() != 0) {
			u = unvisited.get(0);
			
			for (int i = 0; i < verticies.size(); i++)
				if (u != verticies.get(i) && g.getEdge(u, verticies.get(i) ) == null)
					g.insertEdge(u, verticies.get(i));
			
			unvisited.remove(u);
		}
		g.repaint();
	}
} // end AddAllEdges

class RandomWeightAction implements ActionListener {
	private GraphPanel graphPanel;
	private Random rand;
	
	public RandomWeightAction(GraphPanel gp) {
		graphPanel = gp;
		rand = new Random();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		for (int i = 0; i < graphPanel.getEdges().size(); i++) {
			int n = rand.nextInt(20) + 1;
			( (Edge) graphPanel.getEdges().get(i)).setWeight(n);
			graphPanel.repaint();
		}
	}
} // End RandomWeightAction

// Action for the help button
class HelpAction implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		JFrame frame = new JFrame("Help Window");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setResizable(false);
		frame.setLayout(new BorderLayout() );

		JPanel panel = new JPanel();

		StringBuilder sb = new StringBuilder(64);
		
		sb.append("<html> <br>Add Vertex: Click anywhere on the Graph Panel to add a vertex <br>")
		.append("<br>Add Edge: Click on two verticies to add an edge. Will thorw an exception if two vertices already share an edge <br>")
		.append("<br>Move Vertex: Click on a vertex to move it to a different location of the graph panel <br>")
		.append("<br>Shortest Path: Click on two verticies to get the shortest distance from the first selected to the second. <br>")
		.append("<br>Change Weight To: Click on two verticies with a shared edge and type an integer into the field to change the weight.<br>")
		.append("<br>Add All Edges: Adds all edges to un-incidented verticies <br>")
		.append("<br>Gives all edges random weights from 1 to 20 <br>")
		.append("<br>Minimal Spanning Tree: TODO (Maybe?) <br>")
		.append("<br>Help: Displays the help button (Which is why you're here right?)</html>");
		
		JLabel helpText = new JLabel(sb.toString());
		helpText.setFont(new Font("Sans Serif", Font.PLAIN, 13) );
		panel.add(helpText);
		frame.add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
} // End HelpAction 