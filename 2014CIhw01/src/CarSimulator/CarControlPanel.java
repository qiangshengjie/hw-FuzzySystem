package CarSimulator;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import obstacle.CarObstacle;
import calcModel.Engine;
import calcModel.fuzzySystem.FuzzySystemFactory;
import calcModel.geneAlgorithm.ui.GeneControl;
import calcModel.psoAlgorithm.ui.PsoControl;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class CarControlPanel extends JPanel implements ActionListener {
	private static CarControlPanel singleton;

	public static CarControlPanel getInstance() {
		if (singleton == null)
			singleton = new CarControlPanel();
		return singleton;
	}

	public JSpinner xSpinner;
	public JSpinner ySpinner;
	public JSpinner sensorSpinner;
	public JTextField d1Text, d2Text, d3Text, bbText;
	public JCheckBox paintAxis;
	public JCheckBox paintGrid;
	public JCheckBox autoTrack;
	public JCheckBox errIgnore;
	public JCheckBox pathDraw;
	public JCheckBox pathRetain;
	public JComboBox mapChoose;
	public JComboBox fuzzyChoose;
	public JToggleButton startTest;
	public JToggleButton GASimbutton;
	public JToggleButton PSOSimbutton;
	public JButton resetTest;
	public JButton addCars;
	public JButton GAbutton;
	public JButton PSObutton;
	public JSlider phiSize;
	public JSlider testRate;
	public JTextArea consoleArea;
	public CarMap carMap;
	public java.util.Timer testTimer = new java.util.Timer("Test Timer");
	public TimerTask testTask;
	//

	public JPanel dataPanel;
	public JPanel settingPanel;
	public JPanel submitPanel;
	public JPanel consolePanel;
	public ActivityPanel graphicPanel;

	private CarControlPanel() {
		consoleArea = new JTextArea();

		consoleArea.setColumns(20);
		consoleArea.setRows(10);

		this.setLayout(new BorderLayout());

		dataPanel = this.createDataPanel();
		this.add(dataPanel, BorderLayout.NORTH);

		// this.add(settingPanel);

		submitPanel = this.createSubmitPanel();
		this.add(submitPanel, BorderLayout.EAST);

		consolePanel = new JPanel();
		consolePanel.setLayout(new BorderLayout());
		consolePanel.add(new JLabel("Console ouput:"), BorderLayout.NORTH);
		consolePanel.add(consoleArea, BorderLayout.CENTER);
		consoleArea.setDocument(new JTextFieldLimit(1024));

		graphicPanel = new ActivityPanel();
		this.add(graphicPanel, BorderLayout.WEST);

		this.setMaximumSize(new Dimension(200, 300));
	}

	public void recordDeltaTheta(double d1, double d2, double d3, double theta) {
		// System.out.printf("%f %f %f %f\n", d1, d2, d3, theta / Math.PI *
		// 180);
		this.graphicPanel.recordData(d1, d2, d3, theta / Math.PI * 180);
	}

	public void updateInfo(Car car, double v1, double v2, double v3, double bb) {
		if (carMap.cars.size() > 1)
			return;
		xSpinner.setValue(car.getX());
		ySpinner.setValue(car.getY());
		d1Text.setText(String.format("%.3f", v1));
		d2Text.setText(String.format("%.3f", v2));
		d3Text.setText(String.format("%.3f", v3));
		bbText.setText(String.format("%.3f", bb));
	}

	public double[] getSensorInfo() {
		double d1 = Double.parseDouble(d1Text.getText());
		double d2 = Double.parseDouble(d2Text.getText());
		double d3 = Double.parseDouble(d3Text.getText());
		double bb = Double.parseDouble(bbText.getText());
		double[] ret = { d1, d2, d3, bb };
		return ret;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		carMap.repaint();
	}

	protected JPanel createSubmitPanel() {
		testRate = new JSlider(0, 100, 33);
		phiSize = new JSlider(0, 360, 90);

		testRate.setOrientation(JSlider.HORIZONTAL);
		testRate.setPaintLabels(true);
		testRate.setPaintTicks(true);
		testRate.setMajorTickSpacing(10);
		testRate.setToolTipText("Test Rate");
		Hashtable labelTable = new Hashtable();
		labelTable.put(new Integer(0), new JLabel("Stop"));
		labelTable.put(new Integer(33), new JLabel("Normal"));
		labelTable.put(new Integer(100), new JLabel("Fast"));
		testRate.setLabelTable(labelTable);

		phiSize.setOrientation(JSlider.HORIZONTAL);
		phiSize.setPaintLabels(true);
		phiSize.setPaintTicks(true);
		phiSize.setMajorTickSpacing(10);
		phiSize.setToolTipText("Car Rotate");
		labelTable = new Hashtable();
		labelTable.put(new Integer(0), new JLabel("0"));
		labelTable.put(new Integer(180), new JLabel("�k"));
		labelTable.put(new Integer(360), new JLabel("2�k"));
		phiSize.setLabelTable(labelTable);

		phiSize.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					int val = (int) source.getValue();
					carMap.cars.get(0).setPhi(val / 180.0 * Math.PI);
					carMap.cars.get(0).theta = val / 180.0 * Math.PI;
					phiField.setValue(val / 180.0 * Math.PI + "");
					carMap.repaint();
				}
			}
		});

		JPanel submitPanel = new JPanel();
		submitPanel.setLayout(new BorderLayout());
		submitPanel.setBorder(new TitledBorder("Submit"));
		submitPanel.add(createEntryFields(), BorderLayout.NORTH);
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(2, 1));
		// p.add(phiSize);
		p.add(testRate);
		p.add(createButtons());
		submitPanel.add(p, BorderLayout.CENTER);
		// submitPanel.add(createButtons(), BorderLayout.SOUTH);
		return submitPanel;
	}

	protected JPanel createDataPanel() {
		SpinnerModel xmodel = new SpinnerNumberModel(0.00, -100, 100, 0.1);
		SpinnerModel ymodel = new SpinnerNumberModel(0.00, -100, 100, 0.1);
		SpinnerModel sensormodel = new SpinnerNumberModel(0.00, -100, 100, 0.1);
		xSpinner = new JSpinner(xmodel);
		ySpinner = new JSpinner(ymodel);
		sensorSpinner = new JSpinner(sensormodel);

		d1Text = new JTextField();
		d2Text = new JTextField();
		d3Text = new JTextField();
		bbText = new JTextField();

		Font displayFont = new Font("Fixedsys", Font.PLAIN, 16);
		d1Text.setEditable(false);
		d2Text.setEditable(false);
		d3Text.setEditable(false);
		bbText.setEditable(false);
		d1Text.setFont(displayFont);
		d2Text.setFont(displayFont);
		d3Text.setFont(displayFont);
		bbText.setFont(displayFont);
		d1Text.setHorizontalAlignment(JTextField.RIGHT);
		d2Text.setHorizontalAlignment(JTextField.RIGHT);
		d3Text.setHorizontalAlignment(JTextField.RIGHT);
		bbText.setHorizontalAlignment(JTextField.RIGHT);
		xSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				Car car = carMap.cars.get(0);
				if (xPosField.getValue() instanceof Double)
					car.setX((Double) s.getValue());
				else
					car.setX((Long) s.getValue());
				carMap.repaint();
			}
		});
		ySpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				Car car = carMap.cars.get(0);
				if (xPosField.getValue() instanceof Double)
					car.setY((Double) s.getValue());
				else
					car.setY((Long) s.getValue());
				carMap.repaint();
			}
		});
		sensorSpinner.setValue(50);
		sensorSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				Car car = carMap.cars.get(0);
				if (xPosField.getValue() instanceof Double)
					car.sensorDeg = (Double) s.getValue();
				else
					car.sensorDeg = (Long) s.getValue();
				carMap.repaint();
			}
		});

		JPanel dataPanel = new JPanel();

		settingPanel = this.createSettingPanel();

		dataPanel.setLayout(new GridLayout(7, 2));
		dataPanel.add(new JLabel("X:"));
		dataPanel.add(xSpinner);
		dataPanel.add(new JLabel("Y:"));
		dataPanel.add(ySpinner);
		dataPanel.add(new JLabel("Head-Sensor:"));
		dataPanel.add(d1Text);
		dataPanel.add(new JLabel("Right-Sensor:"));
		dataPanel.add(d2Text);
		dataPanel.add(new JLabel("Left-Sensor:"));
		dataPanel.add(d3Text);
		dataPanel.add(new JLabel("Collision:"));
		dataPanel.add(bbText);
		dataPanel.add(new JLabel("Sensor-deg:"));
		dataPanel.add(sensorSpinner);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(dataPanel, BorderLayout.WEST);
		panel.add(settingPanel, BorderLayout.EAST);

		panel.setBorder(new TitledBorder("Information"));
		return panel;
	}

	protected JPanel createSettingPanel() {
		paintAxis = new JCheckBox("Axis");
		paintGrid = new JCheckBox("Grid");
		autoTrack = new JCheckBox("Auto Track");
		errIgnore = new JCheckBox("Error Ignore");
		pathDraw = new JCheckBox("Path Draw");
		pathRetain = new JCheckBox("Path Retain");
		String[] mapStrings = { "map0", "map1", "map2", "map3", "map4", "map5",
				"map6" };
		mapChoose = new JComboBox(mapStrings);
		String[] fuzzyStrings = { "CenterOfGravity", "Functional", "MeanOfMax",
				"(Modified)Mean" };
		fuzzyChoose = new JComboBox(fuzzyStrings);

		paintAxis.setSelected(false);
		paintGrid.setSelected(false);
		autoTrack.setSelected(true);

		paintAxis.addActionListener(this);
		paintGrid.addActionListener(this);
		autoTrack.addActionListener(this);
		mapChoose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				String mapfileName = (String) cb.getSelectedItem();
				System.out.println(mapfileName);
				carMap.loadMapFile(mapfileName);
			}
		});
		fuzzyChoose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				String fuzzyName = (String) cb.getSelectedItem();
				carMap.cars.get(0).fuzzySystem = FuzzySystemFactory
						.createFuzzySystem(fuzzyName);
			}
		});

		JPanel settingPanel = new JPanel();
		settingPanel.setLayout(new GridLayout(6, 2));
		settingPanel.add(paintAxis);
		settingPanel.add(paintGrid);
		settingPanel.add(autoTrack);
		settingPanel.add(errIgnore);
		settingPanel.add(pathDraw);
		settingPanel.add(pathRetain);
		settingPanel.add(new JSeparator());
		settingPanel.add(new JSeparator());
		settingPanel.add(new JLabel("Map: "));
		settingPanel.add(mapChoose);
		settingPanel.add(new JLabel("Fuzzy System:"));
		settingPanel.add(fuzzyChoose);
		return settingPanel;
	}

	JSpinner xPosField, yPosField, phiField;

	protected JComponent createEntryFields() {
		JPanel panel = new JPanel(new SpringLayout());

		String[] labelStrings = { "x:", "y:", "phi:" };

		JLabel[] labels = new JLabel[labelStrings.length];
		JComponent[] fields = new JComponent[labelStrings.length];
		int fieldNum = 0;

		SpinnerModel xmodel = new SpinnerNumberModel(0.00, -100, 100, 0.1);
		SpinnerModel ymodel = new SpinnerNumberModel(0.00, -100, 100, 0.1);
		SpinnerModel pmodel = new SpinnerNumberModel(0.00, -100, 100, 0.02);
		xPosField = new JSpinner(xmodel);
		yPosField = new JSpinner(ymodel);
		phiField = new JSpinner(pmodel);
		// Create the text field and set it up.
		// xPosField = new JFormattedTextField(new DecimalFormat("###.###"));
		// xPosField.setColumns(10);
		xPosField.setValue(0.0);
		fields[fieldNum++] = xPosField;

		// yPosField = new JFormattedTextField(new DecimalFormat("###.###"));
		// yPosField.setColumns(10);
		yPosField.setValue(0.0);
		fields[fieldNum++] = yPosField;

		// phiField = new JFormattedTextField(new DecimalFormat("###.###"));
		// phiField.setColumns(10);
		double v = 1.570796f;
		phiField.setValue(v);
		fields[fieldNum++] = phiField;

		// Associate label/field pairs, add everything,
		// and lay it out.

		ChangeListener action = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Car car = carMap.cars.get(0);
				if (xPosField.getValue() instanceof Double)
					car.setX((Double) xPosField.getValue());
				else
					car.setX((Long) xPosField.getValue());
				if (yPosField.getValue() instanceof Double)
					car.setY((Double) yPosField.getValue());
				else
					car.setY((Long) yPosField.getValue());
				if (phiField.getValue() instanceof Double)
					car.setPhi((Double) phiField.getValue());
				else
					car.setPhi((Long) phiField.getValue());
				car.theta = car.getPhi();
				carMap.repaint();
			}
		};
		for (int i = 0; i < labelStrings.length; i++) {
			labels[i] = new JLabel(labelStrings[i], JLabel.TRAILING);
			labels[i].setLabelFor(fields[i]);
			panel.add(labels[i]);
			panel.add(fields[i]);
			if (fields[i] instanceof JSpinner) {
				JSpinner f = (JSpinner) fields[i];
				f.addChangeListener(action);
			}
		}
		int GAP = 10;
		SpringUtilities.makeCompactGrid(panel, labelStrings.length, 2, GAP,
				GAP, // init x,y
				GAP, GAP / 2);// xpad, ypad
		return panel;
	}

	protected JComponent createButtons() {
		// new JPanel(new FlowLayout(FlowLayout.TRAILING));

		startTest = new JToggleButton("Run(Fuzzy)");
		resetTest = new JButton("Reset");
		addCars = new JButton("Add");
		GAbutton = new JButton("Gene Build");
		GASimbutton = new JToggleButton("Run(GeneX)");
		PSObutton = new JButton("PSO Build");
		PSOSimbutton = new JToggleButton("Run(PSO)");
		Font displayFont = new Font("Comic Sans MS", Font.PLAIN, 16);
		startTest.setFont(displayFont);
		resetTest.setFont(displayFont);
		GAbutton.setFont(displayFont);
		GASimbutton.setFont(displayFont);
		PSObutton.setFont(displayFont);
		PSOSimbutton.setFont(displayFont);

		resetTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Car car = carMap.cars.get(0);
				carMap.cars.removeAllElements();
				carMap.cars.add(car);
				carMap.restart();
				if (xPosField.getValue() instanceof Double)
					car.setX((Double) xPosField.getValue());
				else
					car.setX((Long) xPosField.getValue());
				if (yPosField.getValue() instanceof Double)
					car.setY((Double) yPosField.getValue());
				else
					car.setY((Long) yPosField.getValue());
				if (phiField.getValue() instanceof Double)
					car.setPhi((Double) phiField.getValue());
				else
					car.setPhi((Long) phiField.getValue());
				car.theta = car.getPhi();
				carMap.repaint();
			}
		});

		addCars.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Car car = new Car(new Engine());
				carMap.cars.add(car);
				carMap.obstacles.add(new CarObstacle(car));
			}
		});

		GAbutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				double[][] in = graphicPanel.getDataInput();
				double[] out = graphicPanel.getDataOutput();
				GeneControl.getInstance().setGeneEnvironment(in, out);
				GeneControl.getInstance().restartMachine(
						GeneControl.getInstance().getBestGene());
			}
		});

		PSObutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				double[][] in = graphicPanel.getDataInput();
				double[] out = graphicPanel.getDataOutput();
				PsoControl.getInstance().setGeneEnvironment(in, out);
				PsoControl.getInstance().restartMachine(
						PsoControl.getInstance().getBestSand());
			}
		});

		startTest.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ev) {
				if (ev.getStateChange() == ItemEvent.SELECTED) {
					carMap.driveCar(testRate.getValue());
					startTest.setText("STOP");
				} else if (ev.getStateChange() == ItemEvent.DESELECTED) {
					carMap.stopCar();
					startTest.setText("RUN(Fuzzy)");
				}
			}
		});

		GASimbutton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ev) {
				if (ev.getStateChange() == ItemEvent.SELECTED) {
					GeneControl.getInstance().driveCar(testRate.getValue());
					GASimbutton.setText("STOP");
				} else if (ev.getStateChange() == ItemEvent.DESELECTED) {
					GeneControl.getInstance().stopCar();
					GASimbutton.setText("RUN(GeneX)");
				}
			}
		});
		PSOSimbutton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ev) {
				if (ev.getStateChange() == ItemEvent.SELECTED) {
					PsoControl.getInstance().driveCar(testRate.getValue());
					PSOSimbutton.setText("STOP");
				} else if (ev.getStateChange() == ItemEvent.DESELECTED) {
					PsoControl.getInstance().stopCar();
					PSOSimbutton.setText("RUN(PSO)");
				}
			}
		});

		JPanel panel = new JPanel(new GridLayout(3, 2));
		panel.add(startTest);
		panel.add(resetTest);
		panel.add(GASimbutton);
		panel.add(GAbutton);
		panel.add(PSOSimbutton);
		panel.add(PSObutton);
		// panel.add(addCars);
		// Match the SpringLayout's gap, subtracting 5 to make
		// up for the default gap FlowLayout provides.
		int GAP = 10;
		panel.setBorder(BorderFactory.createEmptyBorder(0, 0, GAP - 5, GAP - 5));
		panel.setPreferredSize(new Dimension(240, 100));
		return panel;
	}
}

class SpringUtilities {
	/**
	 * A debugging utility that prints to stdout the component's minimum,
	 * preferred, and maximum sizes.
	 */
	public static void printSizes(Component c) {
		System.out.println("minimumSize = " + c.getMinimumSize());
		System.out.println("preferredSize = " + c.getPreferredSize());
		System.out.println("maximumSize = " + c.getMaximumSize());
	}

	/**
	 * Aligns the first <code>rows</code> * <code>cols</code> components of
	 * <code>parent</code> in a grid. Each component is as big as the maximum
	 * preferred width and height of the components. The parent is made just big
	 * enough to fit them all.
	 * 
	 * @param rows
	 *            number of rows
	 * @param cols
	 *            number of columns
	 * @param initialX
	 *            x location to start the grid at
	 * @param initialY
	 *            y location to start the grid at
	 * @param xPad
	 *            x padding between cells
	 * @param yPad
	 *            y padding between cells
	 */
	public static void makeGrid(Container parent, int rows, int cols,
			int initialX, int initialY, int xPad, int yPad) {
		SpringLayout layout;
		try {
			layout = (SpringLayout) parent.getLayout();
		} catch (ClassCastException exc) {
			System.err
					.println("The first argument to makeGrid must use SpringLayout.");
			return;
		}

		Spring xPadSpring = Spring.constant(xPad);
		Spring yPadSpring = Spring.constant(yPad);
		Spring initialXSpring = Spring.constant(initialX);
		Spring initialYSpring = Spring.constant(initialY);
		int max = rows * cols;

		// Calculate Springs that are the max of the width/height so that all
		// cells have the same size.
		Spring maxWidthSpring = layout.getConstraints(parent.getComponent(0))
				.getWidth();
		Spring maxHeightSpring = layout.getConstraints(parent.getComponent(0))
				.getWidth();
		for (int i = 1; i < max; i++) {
			SpringLayout.Constraints cons = layout.getConstraints(parent
					.getComponent(i));

			maxWidthSpring = Spring.max(maxWidthSpring, cons.getWidth());
			maxHeightSpring = Spring.max(maxHeightSpring, cons.getHeight());
		}

		// Apply the new width/height Spring. This forces all the
		// components to have the same size.
		for (int i = 0; i < max; i++) {
			SpringLayout.Constraints cons = layout.getConstraints(parent
					.getComponent(i));

			cons.setWidth(maxWidthSpring);
			cons.setHeight(maxHeightSpring);
		}

		// Then adjust the x/y constraints of all the cells so that they
		// are aligned in a grid.
		SpringLayout.Constraints lastCons = null;
		SpringLayout.Constraints lastRowCons = null;
		for (int i = 0; i < max; i++) {
			SpringLayout.Constraints cons = layout.getConstraints(parent
					.getComponent(i));
			if (i % cols == 0) { // start of new row
				lastRowCons = lastCons;
				cons.setX(initialXSpring);
			} else { // x position depends on previous component
				cons.setX(Spring.sum(lastCons.getConstraint(SpringLayout.EAST),
						xPadSpring));
			}

			if (i / cols == 0) { // first row
				cons.setY(initialYSpring);
			} else { // y position depends on previous row
				cons.setY(Spring.sum(
						lastRowCons.getConstraint(SpringLayout.SOUTH),
						yPadSpring));
			}
			lastCons = cons;
		}

		// Set the parent's size.
		SpringLayout.Constraints pCons = layout.getConstraints(parent);
		pCons.setConstraint(
				SpringLayout.SOUTH,
				Spring.sum(Spring.constant(yPad),
						lastCons.getConstraint(SpringLayout.SOUTH)));
		pCons.setConstraint(
				SpringLayout.EAST,
				Spring.sum(Spring.constant(xPad),
						lastCons.getConstraint(SpringLayout.EAST)));
	}

	/* Used by makeCompactGrid. */
	private static SpringLayout.Constraints getConstraintsForCell(int row,
			int col, Container parent, int cols) {
		SpringLayout layout = (SpringLayout) parent.getLayout();
		Component c = parent.getComponent(row * cols + col);
		return layout.getConstraints(c);
	}

	/**
	 * Aligns the first <code>rows</code> * <code>cols</code> components of
	 * <code>parent</code> in a grid. Each component in a column is as wide as
	 * the maximum preferred width of the components in that column; height is
	 * similarly determined for each row. The parent is made just big enough to
	 * fit them all.
	 * 
	 * @param rows
	 *            number of rows
	 * @param cols
	 *            number of columns
	 * @param initialX
	 *            x location to start the grid at
	 * @param initialY
	 *            y location to start the grid at
	 * @param xPad
	 *            x padding between cells
	 * @param yPad
	 *            y padding between cells
	 */
	public static void makeCompactGrid(Container parent, int rows, int cols,
			int initialX, int initialY, int xPad, int yPad) {
		SpringLayout layout;
		try {
			layout = (SpringLayout) parent.getLayout();
		} catch (ClassCastException exc) {
			System.err
					.println("The first argument to makeCompactGrid must use SpringLayout.");
			return;
		}

		// Align all cells in each column and make them the same width.
		Spring x = Spring.constant(initialX);
		for (int c = 0; c < cols; c++) {
			Spring width = Spring.constant(0);
			for (int r = 0; r < rows; r++) {
				width = Spring.max(width,
						getConstraintsForCell(r, c, parent, cols).getWidth());
			}
			for (int r = 0; r < rows; r++) {
				SpringLayout.Constraints constraints = getConstraintsForCell(r,
						c, parent, cols);
				constraints.setX(x);
				constraints.setWidth(width);
			}
			x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
		}

		// Align all cells in each row and make them the same height.
		Spring y = Spring.constant(initialY);
		for (int r = 0; r < rows; r++) {
			Spring height = Spring.constant(0);
			for (int c = 0; c < cols; c++) {
				height = Spring.max(height,
						getConstraintsForCell(r, c, parent, cols).getHeight());
			}
			for (int c = 0; c < cols; c++) {
				SpringLayout.Constraints constraints = getConstraintsForCell(r,
						c, parent, cols);
				constraints.setY(y);
				constraints.setHeight(height);
			}
			y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
		}

		// Set the parent's size.
		SpringLayout.Constraints pCons = layout.getConstraints(parent);
		pCons.setConstraint(SpringLayout.SOUTH, y);
		pCons.setConstraint(SpringLayout.EAST, x);
	}
}
