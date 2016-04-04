import java.io.Serializable;

public class User implements Serializable, IUser{
	private String nick;
	private boolean cpu;
	private String oppNick;

	
	public User(String nick, boolean cpu){
		this.nick = nick;
		this.cpu = cpu;
	}
	
	public String getNick(){
		return nick;
	}
	
	public void setNick(String newNick){
		this.nick = newNick;
	}
	public void setOppNick(String oppNick){
		this.oppNick = oppNick;
	}
	public String getOppNick(){
		return oppNick;
	}

	public boolean playWithCPU(){
		return cpu;
	}
}
