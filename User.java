import java.io.BufferedReader;
import java.io.PrintWriter;

public class User
{
	public PrintWriter salida;
	public BufferedReader entrada;
	public String nombreCliente;

	public User(PrintWriter salida, BufferedReader entrada, String nombreCliente)
	{
		this.entrada = entrada;
		this.salida = salida;
		this.nombreCliente = nombreCliente;
	}
}