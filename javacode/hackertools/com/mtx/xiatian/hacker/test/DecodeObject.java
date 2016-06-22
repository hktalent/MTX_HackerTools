package com.mtx.xiatian.hacker.test;


/**
 * java DecodeObject AAAF9QFlAf//////////AAAAcQAA6mAAAAAYUlE9bFBCMe28Nw2dEVsi2/jvDmamiKsFAnlzcgB4cgF4cgJ4cAAAAAwAAAACAAAAAAAAAAAAAAABAHBwcHBwcAAAAAwAAAACAAAAAAAAAAAAAAABAHAG/gEAAKztAAVzcgAdd2VibG9naWMucmp2bS5DbGFzc1RhYmxlRW50cnkvUmWBV/T57QwAAHhwcgAkd2VibG9naWMuY29tbW9uLmludGVybmFsLlBhY2thZ2VJbmZv5vcj57iuHskCAAlJAAVtYWpvckkABW1pbm9ySQALcGF0Y2hVcGRhdGVJAAxyb2xsaW5nUGF0Y2hJAAtzZXJ2aWNlUGFja1oADnRlbXBvcmFyeVBhdGNoTAAJaW1wbFRpdGxldAASTGphdmEvbGFuZy9TdHJpbmc7TAAKaW1wbFZlbmRvcnEAfgADTAALaW1wbFZlcnNpb25xAH4AA3hwdwIAAHj+AQAArO0ABXNyAB13ZWJsb2dpYy5yanZtLkNsYXNzVGFibGVFbnRyeS9SZYFX9PntDAAAeHByACR3ZWJsb2dpYy5jb21tb24uaW50ZXJuYWwuVmVyc2lvbkluZm+XIkVRZFJGPgIAA1sACHBhY2thZ2VzdAAnW0x3ZWJsb2dpYy9jb21tb24vaW50ZXJuYWwvUGFja2FnZUluZm87TAAOcmVsZWFzZVZlcnNpb250ABJMamF2YS9sYW5nL1N0cmluZztbABJ2ZXJzaW9uSW5mb0FzQnl0ZXN0AAJbQnhyACR3ZWJsb2dpYy5jb21tb24uaW50ZXJuYWwuUGFja2FnZUluZm/m9yPnuK4eyQIACUkABW1ham9ySQAFbWlub3JJAAtwYXRjaFVwZGF0ZUkADHJvbGxpbmdQYXRjaEkAC3NlcnZpY2VQYWNrWgAOdGVtcG9yYXJ5UGF0Y2hMAAlpbXBsVGl0bGVxAH4ABEwACmltcGxWZW5kb3JxAH4ABEwAC2ltcGxWZXJzaW9ucQB+AAR4cHcCAAB4/gEAAKztAAVzcgAdd2VibG9naWMucmp2bS5DbGFzc1RhYmxlRW50cnkvUmWBV/T57QwAAHhwcgAhd2VibG9naWMuY29tbW9uLmludGVybmFsLlBlZXJJbmZvWFR085vJCPECAAdJAAVtYWpvckkABW1pbm9ySQALcGF0Y2hVcGRhdGVJAAxyb2xsaW5nUGF0Y2hJAAtzZXJ2aWNlUGFja1oADnRlbXBvcmFyeVBhdGNoWwAIcGFja2FnZXN0ACdbTHdlYmxvZ2ljL2NvbW1vbi9pbnRlcm5hbC9QYWNrYWdlSW5mbzt4cgAkd2VibG9naWMuY29tbW9uLmludGVybmFsLlZlcnNpb25JbmZvlyJFUWRSRj4CAANbAAhwYWNrYWdlc3EAfgADTAAOcmVsZWFzZVZlcnNpb250ABJMamF2YS9sYW5nL1N0cmluZztbABJ2ZXJzaW9uSW5mb0FzQnl0ZXN0AAJbQnhyACR3ZWJsb2dpYy5jb21tb24uaW50ZXJuYWwuUGFja2FnZUluZm/m9yPnuK4eyQIACUkABW1ham9ySQAFbWlub3JJAAtwYXRjaFVwZGF0ZUkADHJvbGxpbmdQYXRjaEkAC3NlcnZpY2VQYWNrWgAOdGVtcG9yYXJ5UGF0Y2hMAAlpbXBsVGl0bGVxAH4ABUwACmltcGxWZW5kb3JxAH4ABUwAC2ltcGxWZXJzaW9ucQB+AAV4cHcCAAB4/gD//gEAAKztAAVzcgATd2VibG9naWMucmp2bS5KVk1JRNxJwj7eEh4qDAAAeHB3RiEAAAAAAAAAAAAJMTI3LjAuMS4xAAt1cy1sLWJyZWVuc6U8r/EAAAAHAAAbWf///////////////////////////////wB4/gEAAKztAAVzcgATd2VibG9naWMucmp2bS5KVk1JRNxJwj7eEh4qDAAAeHB3HQEWYNfJ8RaIywAJMTI3LjAuMS4xpTyv8QAAAAAAeA==
Data Length: 1525
Object found...weblogic.rjvm.ClassTableEntry
Bytes skipped: 118
Bytes left: 1154
Object found...weblogic.rjvm.ClassTableEntry
Bytes skipped: 375
Bytes left: 745
Object found...weblogic.rjvm.ClassTableEntry
Bytes skipped: 784
Bytes left: 196
Object found...weblogic.rjvm.JVMID
Bytes skipped: 1336
Bytes left: 76
Object found...weblogic.rjvm.JVMID
Bytes skipped: 1453
Bytes left: 0
 * @author xiatian
 *
 */
public class DecodeObject
{
	public static void main(String args[]) throws Exception
	{
//		int skip = 0;
//		int remainder = 0;
//		String b64 = args[0];
//		byte[] bytes = Base64.getDecoder().decode(b64);
//		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
//		int origSize = bis.available();
//		System.out.println("Data Length: " + origSize);
//		Object o = null;
//		while (o == null)
//		{
//			try
//			{
//				bis.reset();
//				bis.skip(skip);
//				ObjectInputStream ois = new ObjectInputStream(bis);
//				o = ois.readObject();
//
//				System.out.println("Object found...");
//				System.out.println(o.getClass().getName());
//				System.out.println("Bytes skipped: " + skip);
//				System.out.println("Bytes left: " + bis.available());
//				skip = origSize - bis.available();
//			} catch (StreamCorruptedException ode)
//			{
//				skip = skip + 1;
//				bis.skip(1);
//			} catch (OptionalDataException ode)
//			{
//				bis.skip(1);
//				skip = skip + 1;
//			} catch (ClassNotFoundException cnf)
//			{
//				System.out.println("Object found..." + cnf.getMessage());
//				System.out.println("Bytes skipped: " + skip);
//				System.out.println("Bytes left: " + bis.available());
//				skip = origSize - bis.available();
//			}
//		}
	}
}