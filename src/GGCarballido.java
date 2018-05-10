import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
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
	private int width, height, vertX, vertY, vStoreCap;
	private ButtonPanel buttonPanel;
	private ArrayList<Vertex> verticies;
	private ArrayList<Edge> edges;
	private ArrayList<Vertex> vStore;

	public GraphPanel(int width, int height) {
		setBorder(BorderFactory.createTitledBorder("Graph") );
		setPreferredSize(new Dimension(width, height) );
		setLayout(null);
		
		verticies = new ArrayList<Vertex>();
		edges = new ArrayList<Edge>();
		vStore = new ArrayList<Vertex>(2); // Stores the two verticies seleceted to make an edge
		vStoreCap = 2;
		
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (buttonPanel.vertexButtonIsOn() ) {
					drawVertex(e.getX(), e.getY());
					verticies.add(new Vertex(e.getX(), e.getY()));
				}
				
				else if (buttonPanel.edgeButtonIsOn() ) {
					for (int i = 0; i < verticies.size(); i++) {
						if (findClosestVertex(e.getX(), e.getY(), verticies.get(i)) ) {
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
								    vStore.clear();
								}
							}
						}
					}
				}
			}
		});

		// TODO move Vertex
		addMouseListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent e) {
		
			}
		});
	} // End Constructor
	
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
	
	private void drawEdge(Vertex v1, Vertex v2) {
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
	private int xCord, yCord, vertW, vertH;
	
	public Vertex(int x, int y) {
		xCord = x;
		yCord = y;
		vertW = 20;
		vertH = 20;
	}
	
	// Returns the value of the x Coordinate
	public int getX() { return xCord; }
	
	// Returns the value of the y Coordinate
	public int getY() { return yCord; }
	
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
	}
} // End Vertex

class Edge {
	private int xCord, yCord, midpointX, midpointY, weight;
	private Vertex vertex1, vertex2;
	
	public Edge(Vertex v1, Vertex v2) {
		vertex1 = v1;
		vertex2 = v2;
		midpointX = (v1.getX() + v2.getX() ) / 2;
		midpointY = (v1.getY() + v2.getY() ) / 2;
		weight = 0;
	}
	
	public int getX() { return xCord; }
	
	public int getY() { return yCord; }
	
	public int getWeight() { return weight; }
	
	public void setWeight(int w) { weight = w; }
	
	public void paintEdge(Graphics g) {
		g.setColor(Color.BLUE);
		g.drawLine(vertex1.getX(), vertex1.getY(), vertex2.getX(), vertex2.getY());
		g.setFont(new Font("Sans Serif", Font.PLAIN, 20) );
		g.drawString(Integer.toString(weight), midpointX, midpointY);
	}
} // End Edge

class RandomWeightAction implements ActionListener {
	private GraphPanel graphPanel;
	private Random rand;
	private Graphics g;
	
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
}

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

