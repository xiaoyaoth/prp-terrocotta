
package simulation.runtime;

public class Map
{
	public static int waiting = 15, memory = 10;
	public static int maxRow = 20, maxCol = 27;
	public static char[][] a = {{'*','*','*','*','*','*','*','*','*','*','*','*','*','*','*','*','*','*','*','*','*','*','*','*','*','*','*','*'},
								{'*','~','~','~','~','~','~','~','~','~','*','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~'},
								{'*','~','~','~','~','~','~','~','~','~','*','$','~','*','$','~','*','$','~','*','$','~','*','$','~','*','$','~'},
								{'*','~','~','~','~','~','~','~','~','~','*','A','~','*','C','~','*','E','~','*','I','~','*','O','~','*','S','~'},
								{'*','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~'},
								{'*','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~'},
								{'*','~','~','A','E','E','~','~','E','E','E','~','~','M','M','M','~','~','N','N','N','~','~','V','V','V','~','~'},
								{'*','~','~','A','*','D','~','~','E','*','I','~','~','I','*','M','~','~','N','*','R','~','~','R','*','V','~','~'},
								{'*','~','~','A','*','D','~','~','F','*','I','~','~','I','*','M','~','~','N','*','R','~','~','R','*','V','~','~'},
								{'*','~','~','A','*','D','~','~','F','*','I','~','~','J','*','M','~','~','N','*','Q','~','~','R','*','V','~','~'},
								{'*','~','~','A','*','D','~','~','F','*','I','~','~','J','*','L','~','~','O','*','Q','~','~','R','*','U','~','~'},
								{'*','~','~','A','*','D','~','~','F','*','H','~','~','J','*','L','~','~','O','*','Q','~','~','S','*','U','~','~'},
								{'*','~','~','B','*','D','~','~','F','*','H','~','~','J','*','L','~','~','O','*','Q','~','~','S','*','U','~','~'},
								{'*','~','~','B','*','C','~','~','F','*','H','~','~','J','*','L','~','~','O','*','Q','~','~','S','*','U','~','~'},
								{'*','~','~','B','*','C','~','~','G','*','H','~','~','J','*','L','~','~','O','*','Q','~','~','S','*','U','~','~'},
								{'*','~','~','B','*','C','~','~','G','*','H','~','~','K','*','L','~','~','O','*','P','~','~','S','*','U','~','~'},
								{'*','~','~','B','*','C','~','~','G','*','H','~','~','K','*','K','~','~','P','*','P','~','~','S','*','T','~','~'},
								{'*','~','~','B','C','C','~','~','G','G','G','~','~','K','K','K','~','~','P','P','P','~','~','T','T','T','~','~'},
								{'*','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~'},
								{'*','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~','~'},
								{'*','Z','Z','Z','Z','Z','Z','Y','Y','Y','Y','Y','Y','X','X','X','X','X','X','W','W','W','W','W','W','T','T','T'}};
}