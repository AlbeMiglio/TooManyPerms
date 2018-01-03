package it.mycraft.toomanyperms;

public enum Permissions {
	
  USE_COMMAND("tmp.use"),  
  RELOAD_COMMAND("tmp.reload"),  
  CHECK_COMMAND("tmp.check"),  
  OPCHECK_COMMAND("tmp.opcheck"),
  GROUPCHECK_COMMAND("tmp.groupcheck");
  
  private String perm;
  
  private Permissions(String perm) {
    this.perm = perm;
  }
  
  public String toString() {
    return this.perm;
  }
}

