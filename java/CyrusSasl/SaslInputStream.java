package CyrusSasl;

import java.io.*;

public class SaslInputStream extends InputStream
{
    private static int BUFFERSIZE=4096;
    private byte[] buffer=new byte[BUFFERSIZE];
    private int bufferstart=0;
    private int bufferend=0;
    private int size = 0;

  private GenericClient conn;

  public InputStream in;
    
  public SaslInputStream(InputStream in, GenericClient conn)
  {
    this.in = in;
    this.conn=conn;
  }

  private int buffersize()
  {
      return size;
  }

  public int available() throws IOException
  {
      int ina = in.available();
      if (ina > 1) ina = 1;

      return buffersize() + ina;
  }

  private int contains_char(char ch)
  {
    if (bufferend>=bufferstart)
    {
      for (int lup=bufferstart;lup<bufferend;lup++)
	if (buffer[lup]==ch)
	  return lup-bufferstart;
    } else {
      for (int lup=bufferend;lup<BUFFERSIZE;lup++)
	if (buffer[lup]==ch)
	  return BUFFERSIZE-lup;
      for (int lup=0;lup<bufferstart;lup++)
	if (buffer[lup]==ch)
	  return BUFFERSIZE-bufferstart+lup;
    }

    return -1;
  }

  private void buffer_add(byte[] str,int len)
  {
    if (str==null)
      return;

    byte[] b=str;

    /* xxx this can be optimized */
    for (int lup=0;lup<len;lup++)
    {
      buffer[bufferend]=b[lup];
      bufferend++;
      if (bufferend==BUFFERSIZE)
	bufferend=0;

      size++;
      if (size >= BUFFERSIZE) {
	System.out.println("uh oh. growing buffer!");	
      }
    }
  }

  private void buffer_add(byte[] str)
  {
    buffer_add(str,str.length);
  }

  private void readsome() throws IOException
  {    
    int len=in.available();

    if (len == 0) return;

    if (len > BUFFERSIZE)
	len = BUFFERSIZE;

    byte[]tmp=new byte[len];
    len = in.read(tmp);
    
    if (len>0)
    {
	buffer_add( conn.decode(tmp,len) );
    }

  }
  public void close() throws IOException
  {
      super.close();
  }

  public synchronized void reset() throws IOException
  {
    return;
  }
  public synchronized void mark(int readlimit)
  {
    return;
  }
    
  public boolean markSupported()
  {
    return false;
  }

    /* read a single byte */
  public int read() throws IOException
  {
    int ret;

    if (buffersize()==0)
      readsome();

    if (size == 0) return -1;

    ret=buffer[bufferstart];
    bufferstart++;
    if (bufferstart==BUFFERSIZE)
      bufferstart=0;

    size--;
    return ret;
  }

  public int read(byte b[]) throws IOException
  {
    return read(b,0,b.length);
  }

  public int read(byte b[],
		  int off,
		  int len) throws IOException
  {
    for (int lup=off;lup<len;lup++)
    {
	int c = read();

	if (c == -1) return lup-off;

	b[lup]= (byte) c;
    }

    return len-off;
  }

  public long skip(long n) throws IOException
  {
    if (n<=0) return 0;

    if ( buffersize()<n)
      readsome();

    int skipped=0;

    while (bufferstart!=bufferend)
    {
      bufferstart++;
      if (bufferstart==BUFFERSIZE)
	bufferstart=0;

      skipped++;
      if (skipped==n)
	return n;
    }
    
    return skipped;    
  }

}
