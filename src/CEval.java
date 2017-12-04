
import com.opencsv.CSVReader;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

class CEval extends JFrame {

    static File dbFile;
    static int selectedMetric = 0;
    static ArrayList<ArrayList> clusters = new ArrayList();
    static String title = "Cluster Evaluator";
    static String opentip = "Choose database to evaluate";
    static String metricTip = "Choose cluster evaluation metric";
    static String evaltip = "Evaluate database with chosen method";
    static JTextField separatorField;
    static JTextField encapsulField;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            CEval prog = new CEval();
            prog.setVisible(true);
        });
    }

    public CEval() {
        setTitle(title);
        setLayout(new FlowLayout());
        setSize(new Dimension(490, 70));
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Open DB
        JButton openButton = new JButton("Open");
        openButton.setToolTipText("<html><p width=\"300\">" + opentip + "</p></html>");
        openButton.addActionListener((ActionEvent event) -> {
            setTitle(title + " - Opening");
            OpenDB();
        });
        add(openButton);

        // Separator
        separatorField = new JTextField(",");
        separatorField.setColumns(1);
        add(new JLabel("Separators:"));
        add(separatorField);

        // Field encapsulator
        encapsulField = new JTextField("\'");
        encapsulField.setColumns(1);
        add(encapsulField);

        // Choose Evaluation Metric
        add(new JLabel("Metric:"));
        JComboBox chooseMetricBox = new JComboBox<Integer>();
        chooseMetricBox.addItem("Silhouette Coefficient");
        chooseMetricBox.addItem("Dunn Index");
        chooseMetricBox.setSelectedItem("Silhouette Coefficient");
        chooseMetricBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JComboBox<Integer> combo = (JComboBox<Integer>) event.getSource();
                selectedMetric = (int) combo.getSelectedIndex();
//                System.out.println(selectedMetric);
            }
        });
        add(chooseMetricBox);

        // Evaluate Clustering
        JButton evalButton = new JButton("Evaluate");
        evalButton.setToolTipText("<html><p width=\"300\">" + evaltip + "</p></html>");
        evalButton.addActionListener((ActionEvent event) -> {
            setTitle(title + " - Evaluate");
            ReadCSV();
        });
        add(evalButton);
    }

    private void OpenDB() {
        JFileChooser dbChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "CSV Database File", "csv");
        dbChooser.setFileFilter(filter);
        int returnVal = dbChooser.showOpenDialog(new JFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
//            System.out.println("You chose to open this file: "
//                    + imageChooser.getSelectedFile().getName());
            try {
                dbFile = dbChooser.getSelectedFile();
                setTitle(title + " - " + dbChooser.getName(dbChooser.getSelectedFile()));
            } catch (Exception ex) {
                Logger.getLogger(CEval.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void ReadCSV() {
        //Build reader instance
        //Read data.csv
        //Default seperator is comma
        //Default quote character is double quote
        //Start reading from line number 2 (line numbers start from zero)
        CSVReader reader;
        try {
            reader = new CSVReader(new FileReader(dbFile), separatorField.getText().charAt(0), encapsulField.getText().charAt(0), 1);
            //Read CSV line by line and use the string array as you want
            String[] nextLine;
            try {
                while ((nextLine = reader.readNext()) != null) {
                    if (nextLine != null) {
                        //Verifying the read data here
                        int cluster = Integer.parseInt(nextLine[nextLine.length - 1].substring(7)) - 1;
//                        System.out.println(cluster + " / " +clusters.size());
                        try {
                            clusters.get(cluster);
                        } catch (Exception ex) {
//                            System.out.println("New AL");
                            for (int i = clusters.size(); i < cluster + 1; i++) {
                                clusters.add(new ArrayList());
                            }
                        }
                        int values[] = new int[nextLine.length];
                        for (int i = 0; i < nextLine.length - 1; i++) {
                            try {
                                values[i] = Integer.parseInt(nextLine[i]);
                            } catch (Exception ex) {
                                values[i] = 0;
                            }
                        }
                        clusters.get(cluster).add(values);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(CEval.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CEval.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (selectedMetric == 0) {
            Silhouette();
        } else {
            Dunn();
        }
    }

    private static void Silhouette() {
        double clusterInDist = 0;
        double clusterOutDist = 0;
        for (int c = 0; c < clusters.size(); c++) {
            ArrayList cluster = clusters.get(c);
            ArrayList nextCluster = c + 1 >= clusters.size() ? clusters.get(0) : clusters.get(c + 1);

            double clusterAvgInDist = 0;
            for (Object instance1 : cluster) {
                double avgInDist = 0;
                for (Object instance2 : cluster) {
                    avgInDist += euclidDist(instance1, instance2);
                }
                avgInDist = avgInDist / cluster.size();
                clusterAvgInDist += avgInDist;
            }
            clusterAvgInDist = clusterAvgInDist / cluster.size();
            clusterInDist += clusterAvgInDist;

            double clusterAvgOutDist = 0;
            for (Object instance1 : cluster) {
                double avgOutDist = 0;
                for (Object instance2 : nextCluster) {
                    avgOutDist += euclidDist(instance1, instance2);
                }
                avgOutDist = avgOutDist / cluster.size();
                clusterAvgOutDist += avgOutDist;
            }
            clusterAvgOutDist = clusterAvgOutDist / cluster.size();
            clusterOutDist += clusterAvgOutDist;
        }
        clusterInDist = clusterInDist / clusters.size();
        clusterOutDist = clusterOutDist / clusters.size();
        double maxDist = clusterInDist > clusterOutDist ? clusterInDist : clusterOutDist;
        double SI = (clusterOutDist - clusterInDist) / maxDist;
        System.out.println(SI);
        JOptionPane.showMessageDialog(new JFrame(), "Silhouette Coefficient: " + SI);
    }

    private static void Dunn() {
        double minOutDist = Double.MAX_VALUE;
        double maxInDist = 0;
        for (ArrayList cluster : clusters) {
            for (Object instance1 : cluster) {
                for (ArrayList cluster2 : clusters) {
                    if (cluster != cluster2) {
                        for (Object instance2 : cluster2) {
                            double temp = euclidDist(instance1, instance2);
                            if (temp < minOutDist) {
                                minOutDist = temp;
                            }
                        }
                    }
                }
            }
        }
        for (ArrayList cluster : clusters) {
            for (Object instance1 : cluster) {
                for (Object instance2 : cluster) {
                    if (instance1 != instance2) {
                        double temp = euclidDist(instance1, instance2);
                        if (temp > maxInDist) {
                            maxInDist = temp;
                        }
                    }
                }
            }
        }
        double dunn = minOutDist / maxInDist;
        System.out.println(minOutDist + " / " + maxInDist + " = " + dunn);
        JOptionPane.showMessageDialog(new JFrame(), "Dunn Index: " + dunn);
    }

    private static double euclidDist(Object i1, Object i2) {
        int[] v1 = (int[]) i1;
        int[] v2 = (int[]) i2;
        double result = 0;
        for (int i = 0; i < v1.length; i++) {
            double temp = ((double) v2[i] - (double) v1[i]);
            result += temp * temp;
        }
        result = Math.sqrt(result);
        return result;
    }
}
