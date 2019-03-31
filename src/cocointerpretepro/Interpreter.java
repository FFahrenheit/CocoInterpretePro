package cocointerpretepro;

import java.awt.Font;
import java.awt.Graphics;
import java.util.Hashtable;
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
    Hashtable<String, Integer> vars;
    
    Interpreter()
    {
        this.x = 0;
        this.y = 0; 
        leftArm = 0;
        rightArm = 0;
        rightLeg = 0;
        leftLeg = 0;
        vars = new Hashtable<String, Integer>();
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
                    + "RA[UP/DOWN/STEADY]: Acción brazo derecho\n"
                    + "LA[UP/DOWN/STEADY]: Acción brazo izquierdo\n"
                    + "LL[UP/DOWN/STEADY]: Acción pierna izquierda\n"
                    + "RL[UP/DOWN/STEADY]: Acción pierna derecha\n"
                    + "FOR [var] [inicio] [hasta] [paso]: Ciclo"
                    + "ID [var] [*,/,+/-,%] [=,!] [val]"); 
        });
        this.add(info);
    }
    
    protected void begin()
    {
        String code = console.getText();
        instructions = code.split("\n");
        for (int i = 0; i < instructions.length; i++) 
        {
            System.out.println("Valor de i final: "+i);
            i = nextInstruction(i);
        }
    }
    
    protected int nextInstruction(int i)
    {
        if(isFor(instructions[i]))
        {
            i = i + nextFor(i);
        }
        else if(isIf(instructions[i]))
        {
            i = i + nextIf(i);
        }
        else if(!nextLine(i))
        {
            i = instructions.length;
        }
        return i;
    }    
    
    protected int findEndIf(int k)
    {
        int nextLine = k, counter = 1;
        for (int i = k+1;i < instructions.length; i++) 
        {
            if(instructions[i].equals("["))
            {
                counter++;
            }
            else if(instructions[i].equals("]"))
            {
                counter--;
            }
            if(counter==0)
            {
                return i;
            }
        }
        return nextLine;
    }
    
    protected int nextIf(int k)
    {
        System.out.println("Es if");
        int i = k + 1;
        if(instructions[i].equals("["))
        {
            boolean execute = evaluate(instructions[k]);
            i++;
            if(!execute)
            {
                i = findEndIf(k);
            }
            while(!instructions[i].equals("]") && execute)
            {
                i = nextInstruction(i);
                i++;
            }
        }
        return i-k;
    }
    
    protected boolean evaluate(String s)
    {
        s = s.substring(3, s.length());
        String[] args = s.split(" ");
        if(vars.get(args[0]) == null)
        {
            System.out.println("Variable no encontrada");
            return false;
        }
        Integer varValue = vars.get(args[0]);
        if(args.length==3)
        {
            int value = Integer.parseInt(args[2]);
            if(args[1].equals("="))
            {
                return value == varValue;
            }
            else if(args[1].equals("!"))
            {
                return value != varValue;
            }
        }
        else
        {
            int value = varValue;
            int b = Integer.parseInt(args[2]);
            int c = Integer.parseInt(args[4]);
            switch(args[1])
            {
                case "*":
                    value = varValue * b;
                    break;
                case "+":
                    value = varValue + b;
                    break;
                case "/":
                    value = varValue / b;
                    break;
                case "%":
                    value = varValue % b; 
                    break;
                case "-":
                    value = varValue - b; 
                    break;
            }
            System.out.println(args[0]+"("+varValue+")"+args[1]+args[2]+args[3]+(value==c));
            return (args[3].equals("!"))?  value != c  : value == c ;
        }
        return false;
    }
    
    protected boolean isIf(String s)
    {
        String regex = "^IF \\w{1} [\\=|\\!] [0-9]+$";
        String regex2 = "^IF \\w{1} [\\*|\\\\|\\%|\\+|\\-] [0-9]+ [\\=|\\!] [0-9]+$";
        return s.matches(regex)|| s.matches(regex2);
    }
    
    protected int nextFor(int k)
    {
        int i=0;
        System.out.println("Es for");
        String[] args = getForParameters(instructions[k]);
        vars.put(args[0], 0);
        int begin =  Integer.parseInt(args[1]);
        int end = Integer.parseInt(args[2]);
        int step = Integer.parseInt(args[3]);
        for (int j =begin; j <= end ; j = j + step) 
        {
            vars.replace(args[0], j);
            i = k + 1;
            if(instructions[i].equals("["))
            {
                i++;
                while(i >= instructions.length||!instructions[i].equals("]"))
                {
                    i = nextInstruction(i);
                    i++;
                }
            }
        }
        return i-k;
    }
    
    protected boolean nextLine(int i)
    {
        if(!processInstruction(instructions[i]))
        {
            JOptionPane pop = new JOptionPane();
            pop.showMessageDialog(null, "Error de sintaxis en línea "+(i+1));
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
            case "RLUP":
                rightLeg = 1;
                break;
            case "RLDOWN":
                rightLeg = -1;
                break;
            case "RLSTEADY":
                rightLeg = 0;
                break;
            case "]":
                System.out.println("Tal vez no es error de sintaxis");
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
