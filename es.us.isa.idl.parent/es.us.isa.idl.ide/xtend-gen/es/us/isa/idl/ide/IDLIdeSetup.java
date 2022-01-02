/**
 * generated by Xtext 2.25.0
 */
package es.us.isa.idl.ide;

import com.google.inject.Guice;
import com.google.inject.Injector;
import es.us.isa.idl.IDLRuntimeModule;
import es.us.isa.idl.IDLStandaloneSetup;
import org.eclipse.xtext.util.Modules2;

/**
 * Initialization support for running Xtext languages as language servers.
 */
@SuppressWarnings("all")
public class IDLIdeSetup extends IDLStandaloneSetup {
  @Override
  public Injector createInjector() {
    IDLRuntimeModule _iDLRuntimeModule = new IDLRuntimeModule();
    IDLIdeModule _iDLIdeModule = new IDLIdeModule();
    return Guice.createInjector(Modules2.mixin(_iDLRuntimeModule, _iDLIdeModule));
  }
}