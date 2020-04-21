package it.mycraft.toomanyperms.utils;

public enum Permissions {
	
  USE_COMMAND("tmp.use"),  
  RELOAD_COMMAND("tmp.reload"),  
  CHECK_COMMAND("tmp.check"),  
  OPCHECK_COMMAND("tmp.opcheck"),
  VERSION_COMMAND("tmp.version"),
  GROUPCHECK_COMMAND("tmp.groupcheck");
  
  private String perm;
  
  private Permissions(String perm) {
    this.perm = perm;
  }
  
  public String toString() {
    return this.perm;
  }
}

