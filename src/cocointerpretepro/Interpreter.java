package cocointerpretepro;

import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

/**
 * Aplicación que lee instrucciones
 * de un texto y realiza operaciones
 * @author ivan_
 */
public class Interpreter extends JFrame
{
    protected int x;
    protected int y;
    protected int leftArm; /* -1: abajo, 0: normal, +1: arriba */
    protected int rightArm;
    protected int leftLeg;
    protected int rightLeg;
    protected JTextArea console; 
    protected JButton play;
    protected JButton info;
    private String[] instructions;
    
    Interpreter()
    {
        this.x = 0;
        this.y = 0; 
        leftArm = 0;
        rightArm = 0;
        rightLeg = 0;
        leftLeg = 0;
        initWindow();
        initComponents();
        this.setLayout(null);
    }
    
    protected void initWindow()
    {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(635, 480);
        this.setTitle("CocoInterpretePro");
        this.getContentPane().setBackground(java.awt.Color.black);
    }
    
    protected void initComponents()
    {
        console = new JTextArea();
        Font font = new Font("Consolas",Font.PLAIN,15);
        console.setFont(font);
        console.setBounds(400, 10, 200 , 350);
        this.add(console);
        
        play = new JButton("Play");
        play.setBackground(java.awt.Color.white);
        play.setBounds(400,375,90,40);
        play.addActionListener(e->
        {
            Thread thread = new Thread() 
            {
                @Override
                public void run() 
                {
                    begin();
                }
            };
            thread.start();
        });
        this.add(play);
        
        info = new JButton("Help");
        info.setBackground(java.awt.Color.white);
        info.setBounds(510,375,90,40);
        info.addActionListener(e->
        {
            JOptionPane pop = new JOptionPane();
            pop.showMessageDialog(null, "Resumen de funciones y sintaxis:\n"
                    + "UP: Mueve hacia arriba\n"
                    + "DOWN: Mueve hacia abajo\n"
                    + "LEFT: Mueve hacia la izquierda\n"
                    + "RIGHT: Mueve hacia la derecha\n"
                    + "RAUP: Brazo derecho arriba\n"
                    + "RADOWN: Brazo derecho abajo\n"
                    + "RASTEADY: Brazo derecho estático\n"
                    + "LAUP: Brazo derecho arriba\n"
                    + "LADOWN: Brazo izquierdo abajo\n"
                    + "LASTEADY: Brazo izquierdo estático\n"
                    + "FOR [var] [inicio] [hasta] [paso]: Ciclo"); 
        });
        this.add(info);
    }
    
    protected void begin()
    {
        String code = console.getText();
        instructions = code.split("\n");
        for (int i = 0; i < instructions.length; i++) 
        {
            if(isFor(instructions[i]))
            {
                i = i + nextFor(i);
            }
            else if(!nextLine(i))
            {
                break;
            }
        }
    }
    
    protected int nextFor(int i)
    {
        int counter=0;
        System.out.println("Es for");
        String[] args = getForParameters(instructions[i]);
        int begin =  Integer.parseInt(args[1]);
        int end = Integer.parseInt(args[2]);
        int step = Integer.parseInt(args[3]);
        for (int j =begin; j < end ; j = j + step) 
        {
            counter = 1;
            if(instructions[i+counter].equals("["))
            {
                counter++;
                do
                {
                    nextLine(i+counter);
                    counter++;
                }while(!instructions[i+counter].equals("]"));    
            }
        }
        return counter;
    }
    
    protected boolean nextLine(int i)
    {
        if(!processInstruction(instructions[i]))
        {
            JOptionPane pop = new JOptionPane();
            pop.showMessageDialog(null, "Error de sintaxis en línea "+i+1);
            return false;
        }
        else
        {
            this.repaint();
            try 
            {
                Thread.sleep(450);
            }
            catch(InterruptedException e) 
            {
                System.out.println("Error de delay");
            }
        }
        return true;
    }
    
    protected boolean isFor(String s)
    {
        String regex = "^FOR \\w{1} [0-9]+ [0-9]+ [0-9]+$";
        return s.matches(regex);
    }
    
    protected String[] getForParameters(String s)
    {
        s = s.substring(4,s.length());
        return s.split(" ");
    }
    
    protected boolean processInstruction(String instruction)
    {
        switch(instruction)
        {
            case "UP":
                y-=10;
                break;
            case "DOWN":
                y+=10;
                break;
            case "LEFT":
                x-=10;
                break;
            case "RIGHT":
                x+=10;
                break;
            case "RAUP":
                rightArm = 1;
                break;
            case "RADOWN":
                rightArm = -1;
                break;
            case "RASTEADY":
                rightArm = 0;
                break;
            case "LAUP":
                leftArm = 1;
                break;
            case "LADOWN":
                leftArm = -1;
                break;
            case "LASTEADY":
                leftArm = 0;
                break;
            case "LLUP":
                leftLeg = 1;
                break;
            case "LLDOWN":
                leftLeg = -1;
                break;
            case "LLSTEADY":
                leftLeg = 0;
                break;
            case "RRUP":
                rightLeg = 1;
                break;
            case "RRDOWN":
                rightLeg = -1;
                break;
            case "RRSTEADY":
                rightLeg = 0;
                break;
            default:
                return false;
        }
        return true;
    }
    
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        g.setColor(java.awt.Color.white);
        g.drawOval(200+x, 180+y, 50, 50); //Head 
        g.drawLine(225+x,230+y,225+x,300+y); //Torso
        g.drawLine(225+x,250+y,260+x,250+y - rightArm*30); //Brazo derecho
        g.drawLine(190+x, 250+y - leftArm*30, 225+x, 250+y); //Brazo izquierdo
        g.drawLine(225+x, 300+y, 200+x-leftLeg*25, 350+y ); //Pierna izquierda
        g.drawLine(225+x, 300+y, 250+x+rightLeg*25, 350+y ); //Pierna derecha
    }
}
