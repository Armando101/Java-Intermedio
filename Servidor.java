import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.*;
import java.util.ArrayList;

public class Servidor extends Thread 
{
    private String nombreCliente;
    private BufferedReader entrada;
    private static DataOutputStream salidaCliente;
    private PrintWriter salida;
    private DataInputStream inA;
    private DataOutputStream outA;
    private static Socket cliente;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private BufferedInputStream bin;
    static ArrayList<User> users = new ArrayList<User>();

 
    public void enviar(String ruta) {
     int contador = 0;
        int[] datos = new int [16936];
        
        try {
            FileInputStream archivo_lectura = new FileInputStream(ruta);
            boolean final_archivo = false;
            while(!final_archivo){
                int byte_entrada = archivo_lectura.read();
                
                if (byte_entrada != -1) {
                    datos[contador] = byte_entrada;
                }
                
                if (byte_entrada == -1) {
                    final_archivo = true;
                }
                contador++;
                //System.out.println(datos[contador]);
            }
            System.out.println(contador-1);
            archivo_lectura.close();
        }catch(IOException e){}
        crea_archivo(datos, ruta);   
    }

    public void crea_archivo(int [] datos, String ruta) {
        try {
            FileOutputStream nuevaimagen = new FileOutputStream(ruta+"_Enviado");
            for (int i = 0; i < datos.length; i++) {
                nuevaimagen.write(datos[i]);
            }
        }catch (IOException e) {}
        
    }

    public Servidor(Socket s) throws IOException 
    {
        entrada = new BufferedReader(new InputStreamReader(s.getInputStream()));
        nombreCliente = s.getInetAddress().getCanonicalHostName();
        salida = new PrintWriter(s.getOutputStream(), true);
        users.add(new User(salida, entrada, nombreCliente));
        in = new ObjectInputStream(s.getInputStream());
        out = new ObjectOutputStream(s.getOutputStream());
        //String nombreUsuario = s.username;
        System.out.println(nombreCliente);
        inA = new DataInputStream(s.getInputStream());
        outA = new DataOutputStream(s.getOutputStream());
        nuevo("Conexión aceptada desde " + s.getRemoteSocketAddress());
    }
    
    public String recibir() throws IOException 
    {
        String str = 
        //inA.readUTF();
        entrada.readLine();
        return str;
    }
    
    public void cerrar() throws IOException 
    {
        entrada.close();
    }

    public void allAreOne(String cadena)
    {
        for (int i = 0; i < users.size(); i++) 
        {
            User user = users.get(i);
            if(user == null || user.salida == null)
            {
                users.remove(user);
            }
            user.salida.println(nombreCliente + " dice: " + cadena);
            //System.out.println(cadena);
            //usuario.salida.println(cadena);
        }
    }

    public void archivo()
    {
        File transferFile = new File (System.getProperty("user.home") + "/Descargas/RdM.jpeg");
        byte [] bytearray  = new byte [(int)transferFile.length()];
        try {
            bin = new BufferedInputStream(new FileInputStream(transferFile));
            bin.read(bytearray,0,bytearray.length);

            outA.write(bytearray,0,bytearray.length);
            outA.flush();
            bin.close();
            outA.close();
        }
        catch (Exception e) {

        }
        /*
        byte[] bytes = new byte[Integer.parseInt(transferFile.length())];
        inA.read(bytes);
        try {
            outA.write(bytes);
            outA.close();
        }
        catch (Exception ex) {
            System.out.println("error telling everyone");
        }*/
    }

    public void nuevo(String cadena)
    {
        for (int i = 0; i < users.size(); i++) 
        {
            User user = users.get(i);
            if(user == null || user.salida == null)
            {
                users.remove(user);
            }
            user.salida.println(cadena);
            //System.out.println(cadena);
            //usuario.salida.println(cadena);
        }
    }

    public void quitar(String cadena) throws IOException
    {
        for (int i = 0; i < users.size(); i++) 
        {
            User user = users.get(i);
            if(user.nombreCliente.equals(cadena))
            {
                user.salida.println("Te han sacado del chat");
                user.salida.println("salir");
                user.entrada.close();
                users.remove(user);
            }
        }
    }

    public void privado(String target, String mensaje) throws IOException
    {
        for (int i = 0; i < users.size(); i++) 
        {
            User user = users.get(i);
            System.out.println(user.nombreCliente);
            if(user.nombreCliente.equals(target))
            {
                user.salida.println(nombreCliente + " dice por mensaje privado:" + mensaje);
            }
        }
    }


    public void listar(String cadena) throws IOException
    {
        String list = "LIST";
        if(cadena == list)
        {
            for (int i = 0; i < users.size(); i++) 
            {
                User user = users.get(i);
                if(user == null || user.salida == null)
                {
                    users.remove(user);
                }
                salida.println(user.nombreCliente);
            }
        }
    }
    
    @Override
    public void run() 
    {
        try 
        {
            String cadena = "";
            while(true) {   //El programa no dejara hacer ninguna accion
                cadena = recibir();
                if (cadena.equals("ADD")) {
                    allAreOne(nombreCliente + " Se ha unido al club");
                    break;//Solo presionando ADD deja hacer las demas acciones
                } else {
                    allAreOne(nombreCliente+" necesita Ingresa ADD");
                }
            }

            do 
            {
                cadena = recibir();
                if (cadena.equals("salir"))
                {
                    salida.println(nombreCliente + " ha dejado la sala de chat");
                }
                else if(cadena.contains("QUIT"))
                {
                    cadena = cadena.replace("QUIT", "");
                    cadena = cadena.replace(" ", "");
                    try
                    {
                        quitar(cadena);
                    }
                    catch (Exception e) {
                        salida.println("Comando no reconocido (QUIT)");
                    }
                }
                else if (cadena.contains("LIST")) 
                {
                    cadena = "LIST";
                    listar(cadena);
                }
                else if (cadena.contains("TEXT"))
                {
                    if(cadena.contains("TEXT_TO"))
                    {
                        cadena = cadena.replace("TEXT_TO", "");
                        cadena = cadena.replace(" ", "");
                        String target = "";
                        salida.println("A quien?");
                        target = recibir();
                        System.out.println(target);
                        String mensaje = "";
                        salida.println("Que mensaje?");
                        mensaje = recibir();
                        privado(target, mensaje);
                    }
                    else
                    {
                        cadena = cadena.replace("TEXT", "");
                        //cadena = cadena.replace(" ", "");
                        allAreOne(cadena);
                    }
                }
                else if (cadena.contains("SEND_FILE")) 
                {
                    enviar("/home/armando/Descargas/RdM.jpeg");
                    allAreOne("He enviado un archivo");
                    //archivo();
                }
            } while (!cadena.equals("salir"));
            
        } 
        catch (IOException e) 
        {
            allAreOne("Se cerró la conexión con " + nombreCliente);
        }
        catch (NullPointerException e) {
         allAreOne("Se cerró la conexión con " + nombreCliente);
     } 
     finally 
     {
        try 
        {
            cerrar();
        } 
        catch (IOException e) {}
    }
}

public static void main(String[] args) throws IOException {
    ServerSocket ss;
    ss = new ServerSocket(9999);
    System.out.println("Servidor aceptando onexiones en el puerto " + ss.getLocalPort());
    while (true) 
    {
        cliente = ss.accept();
        Servidor hilo = new Servidor(cliente);

        hilo.start();
    }
}
}