package simulation.modeling;

import java.util.ArrayList;

public class Path {
	private ArrayList<Integer> path = new ArrayList<Integer>();

	public Path() {
	}

	public Path(int id) {
		this.path.add(id);
	}

	public Path(Path p) {
		for (int i = 0; i < p.getSize(); i++)
			this.path.add(p.get(i));
	}

	public Path(Path p, int id) {
		this(p);
		this.path.add(id);
	}

	public void add(int id) {
		this.path.add(id);
	}

	public void remove(int index) {
		this.path.remove(index);
	}

	public void set(int index, int id) {
		this.path.set(index, id);
	}

	public int get(int index) {
		return this.path.get(index);
	}

	public int getSize() {
		return this.path.size();
	}

	public String toString() {
		if (this.path.size() == 0)
			return "";
		String s = "" + this.path.get(0);
		for (int i = 1; i < this.path.size(); i++)
			s += "-" + this.path.get(i);
		return s;
	}
	
	public boolean equals(Path p)
	{
		if (p.getSize() != this.getSize()) return false;
		else for (int i=0; i<this.getSize(); i++)
		{
			if (this.get(i) != p.get(i)) return false;
		}
		return true;
	}
}
