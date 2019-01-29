package sicassembler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class SICASSEMBLER {
    // load and store
    public static final int LDA = 0x00;
    public static final int LDX = 0x04;
    public static final int LDL = 0x08;
    public static final int STA = 0x0C;
    public static final int STX = 0x10;
    public static final int STL = 0x14;
    // fixed point arithmetic
    public static final int ADD = 0x18;
    public static final int SUB = 0x1C;
    public static final int MUL = 0x20;
    public static final int DIV = 0x24;
    public static final int COMP = 0x28;
    public static final int TIX = 0x2C;
    // jumps
    public static final int JEQ = 0x30;
    public static final int JGT = 0x34;
    public static final int JLT = 0x38;
    public static final int J = 0x3C;
    // bit manipulation
    public static final int AND = 0x40;
    public static final int OR = 0x44;
    // jump to subroutine
    public static final int JSUB = 0x48;
    public static final int RSUB = 0x4C;
    // load and store int
    public static final int LDCH = 0x50;
    public static final int STCH = 0x54;
    
    

   public static HashMap<String,Integer> optabINIT(HashMap <String, Integer> optab){
        optab.put("STCH",STCH);optab.put("LDCH",LDCH);optab.put("RSUB",RSUB);
        optab.put("JSUB",JSUB);optab.put("SUB",SUB);optab.put("LDX",LDX);
        optab.put("STA",STA);optab.put("STX",STX);optab.put("LDL",LDL);
        optab.put("DIV",DIV);optab.put("MUL",MUL);optab.put("STL",STL);
        optab.put("COMP",COMP);optab.put("TIX",TIX);optab.put("JEQ",JEQ);
        optab.put("JGT",JGT);optab.put("JLT",JLT);optab.put("J",J);
        optab.put("AND",AND);optab.put("OR",OR);optab.put("LDA",LDA);
        optab.put("RD",0xD8);optab.put("TIX",0x2C);optab.put("WD",0xDC);
        optab.put("TD",0xE0);
        for(Object s : optab.keySet().toArray()){
            String key = (String)s;
            key+= ",x";
            int val;
            val = optab.get(s);
            optab.put(key,val);
        }
        return optab;
    }
    public static void main(String[] args) throws IOException {
        String inputFile ="input.txt";
        String intermediateFile ="intermediate.txt";
        String outputFile ="output.txt";
        String [] tokens;
        String startAdress = null;
        int LCCTR = 0;
        int oldLCCTR = 0;
        PrintWriter writer = null;
        boolean firstLine = true;
        String ProgName = null;
        HashMap <String, Integer> symtab = new HashMap <String, Integer>();
        HashMap <String, Integer> optab = new HashMap <String, Integer>();
        optab = optabINIT(optab);
       // System.out.println(optab.get("JEQ"));
        int err=0; // 1 means duplicated symbol , 2 means invalid op
        int programLength = 0;
        
        try(BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
             
            for(String line; (line = br.readLine()) != null; ) { //read first input line
               //   System.out.println(line);
               
                int i=0;
                tokens = line.split("	");
                for(String s : tokens){
                    System.out.println(s);
                 //   System.out.println(i++);
                }
                if (firstLine){
                    if(tokens[1].equals("START")){ //If OPCODE - ‘START’ :
                        startAdress = tokens[2];
                        System.out.println("start:  "+startAdress);
                        System.out.println("tok:  "+tokens[2]);
                        LCCTR = Integer.parseInt(startAdress);//initialize LOCCTR to starting address
                        ProgName = tokens[1];
                        writer = new PrintWriter(intermediateFile, "UTF-8");
                       // writer.println(line);//write line to intermediate file
                        writer.close();
                    }
                    else{
                        LCCTR = 0; //initialize LOCCTR to 0
                    }
                }
                oldLCCTR = LCCTR;
                if(!tokens[0].equals(".")){//if this is not a comment line :
                    if(!tokens[0].equals("")){//   if there is a symbol in the LABEL field 
                        if(symtab.containsKey(tokens[0])){ //search SYMTAB for LABEL
                            err=1;
                        } 
                        else{
                            symtab.put(tokens[0], LCCTR);//insert (LABEL,LOCCTR) into SYMTAB
                        }
                    }
                    if(optab.containsKey(tokens[1])){ //search 0PTAB for OPCODE
                        LCCTR += 3; //add 3 (instruction length) to LOCCTR
                    } 
                    else if (tokens[1].equals("WORD")){
                        LCCTR+=3; //add 3 (word length) to LOCCTR
                    }
                    else if (tokens[1].equals("RESW")){
                        LCCTR+=3 * Integer.parseInt(tokens[2]);// add 3 * #[OPERAND] to LOCCTR
                    }
                    else if (tokens[1].equals("RESB")){
                        LCCTR+= Integer.parseInt(tokens[2]); //add #[OPERAND] to LOCCTR
                    }
                    else if (tokens[1].equals("BYTE")){ //add length to LOCCTR 
                        LCCTR+= tokens[2].length();
                    }
                    else{
                        err =2;
                    }
                }
                //write line to intermediate file
                writer = new PrintWriter(new FileOutputStream(new File(intermediateFile),true));
                writer.println(oldLCCTR+"	"+line);
                writer.close();
                //save (LOCCTR - starting address) as program length
                programLength = LCCTR - Integer.parseInt(startAdress);
             /*   System.out.println(programLength);
                System.out.println(symtab.toString());
                System.out.println(optab.toString());*/
                firstLine = false;
                
            }
            
        } 
        catch (FileNotFoundException ex) {
            Logger.getLogger(SICASSEMBLER.class.getName()).log(Level.SEVERE, null, ex);
        }
        //----------------------------end of pass 1 -----------------------------------
        pass2(intermediateFile, programLength ,outputFile , optab , symtab );
        try(BufferedReader br = new BufferedReader(new FileReader("output.txt"))) {
            writer = new PrintWriter(new FileOutputStream(new File("output2.txt"),false));
                writer.close();
            for(String line; (line = br.readLine()) != null; ) {
                String hi = " ";
                tokens = line.split(" ");
                if(tokens[0].equals("T")){
                    
                    if(tokens[tokens.length-1].equals("000000")){
                        int x = tokens.length - 3;
                        int y = x*3;
                        
                        hi+=tokens[0];hi+=" ";
                        hi+=Integer.toHexString(y).toUpperCase();hi+=" ";
                        int l;
                        for(l=2;l<tokens.length-1;l++){
                            if(l==2){}
                            else{
                        hi+= tokens[l];
                        hi+=" ";
                            }
                        
                        
                        }
                    }
                        else{
                        int x = tokens.length - 3;
                        int y = x*3;
                        hi+=tokens[0];hi+=" ";
                        hi+=Integer.toHexString(y).toUpperCase();hi+=" ";
                        int l;
                        for(l=2;l<tokens.length;l++){
                            if(l==2){}
                            else{
                        hi+= tokens[l];
                        hi+=" ";
                            }
                        }
                        
                    }
                   writer = new PrintWriter(new FileOutputStream(new File("output2.txt"),true));
                        writer.println(hi);
                        writer.close();
                }
                else if (tokens[0].equals("H")){
                writer = new PrintWriter(new FileOutputStream(new File("output2.txt"),true));
                        writer.println(line);
                        writer.close();
                        System.out.println("a7a");

                }
            }
            writer = new PrintWriter(new FileOutputStream(new File("output2.txt"),true));
                        writer.println("E   "+fixer(Integer.toHexString(Integer.parseInt(startAdress))));
                        writer.close();
        }
        
    }
    static void pass2 (String intermediateFile , int progL , String outputFile , HashMap<String,Integer> optab , HashMap<String,Integer> symtab ) throws FileNotFoundException, IOException
{    
  System.out.println(symtab.get("RDREC"));      
  //  System.out.println(GetObjcode("5168		STCH	BUFFER,X", optab , symtab));
    boolean rowStarted = true ; 
    ArrayList<String> Ops = new ArrayList<String>();

    int instructions_num = 0;
    int counter = 0;
    String [] tokenz;
String startAddy = null;
      PrintWriter writer  = new PrintWriter(outputFile, "UTF-8");
       writer.close();
       
 BufferedReader br = new BufferedReader(new FileReader(intermediateFile)) ;
   
        
            for(String line; (line = br.readLine()) != null; )
   
                { 
                    tokenz = line.split("	");
                    if(tokenz.length >= 2 && !tokenz[1].equals(".")   )
                       Ops.add(tokenz[2]);
        
                }
            String[] operations = new String[Ops.size()];
            operations = Ops.toArray(operations);
           Integer[] row_sizes =  size(operations);
        
            
 
         br = new BufferedReader(new FileReader(intermediateFile)) ;
                    int i =0;
                    int j=0;
            for(String line; (line = br.readLine()) != null; ) {
                
                
                 tokenz = line.split("	");
                     
              
                if( tokenz.length == 4 && tokenz[2].equals("START")  )
                 {     
                     startAddy =  tokenz[3];
                writer = new PrintWriter(new FileOutputStream(new File(outputFile),true));
                writer.println("H"+ " " +(tokenz[1]) + " " + fixer(tokenz[3]) + " " + fixer(Integer.toHexString(progL)) );
                writer.close(); 
                 }
                else if (tokenz.length == 3 && tokenz[2].equals("END"))
                {
                writer = new PrintWriter(new FileOutputStream(new File(outputFile),true));
                 writer.print(System.lineSeparator()+ "E"+ " " + fixer(startAddy) );               
                writer.close(); 
                    
                }
                else if (!tokenz[1].equals(".") &&tokenz.length >3 && !tokenz[2].equals("WORD")&& !tokenz[2].equals("BYTE") && !tokenz[2].equals("RESW") &&!tokenz[2].equals("RESB") )
                {
                    
                  
                //  System.out.println(GetObjcode(line,optab,symtab));
                  if(rowStarted)
                    {
                writer = new PrintWriter(new FileOutputStream(new File(outputFile),true));
                
                writer.print("T"+ " " + fixer(tokenz[0]).toUpperCase() +  " " +  fixer2(Integer.toHexString(row_sizes[i])).toUpperCase() + " " + GetObjcode(line,optab,symtab).toUpperCase() )    ;
                
                writer.close(); 
                rowStarted= false;
                    }
                  else if (j < row_sizes[i]-1)
                    {
                        System.out.println(row_sizes[i]);
                        writer = new PrintWriter(new FileOutputStream(new File(outputFile),true));
                       writer.print(" " + fixer(GetObjcode(line,optab,symtab)));
                       j++;
                      writer.close(); 
                        
                        
                    }
                  else if(i <  row_sizes.length)
                  {
                       writer = new PrintWriter(new FileOutputStream(new File(outputFile),true));
                                      writer.print(System.lineSeparator() );
                                      writer.close(); 

                      rowStarted = true;
                      j=0;
                      i++;
                      
                  }
                 
                }
                
               
                 
                 
                 
                 
                 
                
                
    }
  
    
    

}
    static String GetObjcode(String line , HashMap<String,Integer> optab  , HashMap<String,Integer> symtab  )
    {
        StringBuilder OC = new StringBuilder(24);

         String [] tokens = line.split("	"); 
         System.out.println("lala:  "+optab.get(tokens[2]));
        try {OC.append( Integer.toHexString(optab.get(tokens[2])).toUpperCase());}
        catch(Exception e){
        
        }
        if(tokens.length >3 && line.contains(",X")) //X indexed
        {
            
             tokens[3] = tokens[3].replace(",X", "");
             OC.append( fixer4((hexer(fixer_16_1(Integer.toBinaryString(symtab.get(tokens[3])))))));
             
             
             
             
        }
        else if (tokens.length >3 )
        {
            //System.out.println(hexer(fixer16_0(Integer.toBinaryString(symtab.get(tokens[3] )))));
           try{OC.append(fixer4(hexer(fixer16_0(Integer.toBinaryString(symtab.get(tokens[3]))))));}
           catch(Exception e){
           }
                    
        }
        
        
        
         
         
         return OC.toString();
        
    }
    
    
    public static String fixer(String fixme) {
     

        if (fixme.length() < 6) {
            fixme = ("000000" + fixme).substring(fixme.length());

        }
        return fixme;

    }
    public static String fixer16_0(String fixme) {
       

        if (fixme.length() < 16) {
            fixme = ("0000000000000000" + fixme).substring(fixme.length());

        }
        return fixme;

    }
    public static String fixer_16_1(String fixme) {
    

        if (fixme.length() < 16) {
            fixme = ("000000000000000" + fixme).substring(fixme.length());
            fixme = ("1" + fixme);
        }
        return fixme;

    }
    public static Integer[] size(String[] countme) {

        List<Integer> sum = new ArrayList<>();
        int counter = 0;
        int j = 0;
        int i = 0;
        for (i = 0; i < countme.length; i++) {
            if (countme[i] != "RESW" && countme[i] != "RESB" && counter < 10) {
                counter++;
               // System.out.println(counter);
            } else {
                {

                    sum.add(j, counter);
                    if (counter < 10) {
                        sum.add(j + 1, 999);
                        counter = 0;

                        j += 2;
                    } else {
                        counter = 1;

                        j++;
                    }
                }

            }
        }
        if (countme.length == i) {
            sum.add(j, counter);
            counter = 0;
            j++;
        }

        Integer[] sumArray = sum.toArray(new Integer[0]);

        return sumArray;
    }
    public static String hexer(String hexme)
    {
        String temp = Long.toHexString(Long.parseLong(hexme,2));
        return temp.toUpperCase();
    
    }
public static String fixer4(String fixme) {
        

        if (fixme.length() < 4) {
            fixme = ("0000" + fixme).substring(fixme.length());
            

        }
        return fixme;

    }
public static String fixer2(String fixme) {
      //  String fixed = null;

        if (fixme.length() < 2) {
            fixme = ("00" + fixme).substring(fixme.length());
            

        }
        return fixme;

    }
   
}


/*


begin
    read first input line {from intermediate file}
    If OPCODE - ‘START’ :
        write listing line
        read next input line
    write Header record to object program
    initialize first Text record
    while OPCODE != ‘END’ :
        if this is not a comment line :
            search OP'I‘AB for OPCODE
            If found :
                if there is a symbol in OPERAND field :
                    search SMAB for OPERAND
                    if found then
                    store symbol value as operand address
                    else:
                    store 0 as operand address
                    Set error flag (undefined symbol)
                Else:
                    store 0 no operand add»…
                    Assemble the object code instruction
            else if OPCODB = ‘BYTE' or ‘WORD‘ :
                convert constant to object code
            if obj code will not fit into the current Text record :
                write Text record to object program
                initialize new Tact record
            add object code to Text record
        write listing line
        read next input line
    write last Text record to object program
    write End record to object program
    write last listing line
end (Pass 2)



*/