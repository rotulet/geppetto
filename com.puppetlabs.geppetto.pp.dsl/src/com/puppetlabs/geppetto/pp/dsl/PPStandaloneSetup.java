/**
 * Copyright (c) 2013 Puppet Labs, Inc. and other contributors, as listed below.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Puppet Labs
 */
package com.puppetlabs.geppetto.pp.dsl;

import java.util.Map;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.xtext.resource.IResourceServiceProvider;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.puppetlabs.geppetto.pp.PPFactory;
import com.puppetlabs.geppetto.pp.dsl.pptp.PptpRuntimeModule;

/**
 * Initialization support for running Xtext languages
 * without equinox extension registry
 */
public class PPStandaloneSetup extends PPStandaloneSetupGenerated {

	public static void doSetup() {
		new PPStandaloneSetup().createInjectorAndDoEMFRegistration();
	}

	private Injector pptpInjector;

	@Override
	public Injector createInjectorAndDoEMFRegistration() {
		// These must be registered when running in a non-OSGi environment
		Registry registry = EPackage.Registry.INSTANCE;
		if(!registry.containsKey(org.eclipse.xtext.XtextPackage.eNS_URI))
			registry.put(org.eclipse.xtext.XtextPackage.eNS_URI, org.eclipse.xtext.XtextPackage.eINSTANCE);

		if(!registry.containsKey(com.puppetlabs.geppetto.pp.PPPackage.eNS_URI))
			registry.put(com.puppetlabs.geppetto.pp.PPPackage.eNS_URI, com.puppetlabs.geppetto.pp.PPPackage.eINSTANCE);

		if(!registry.containsKey(com.puppetlabs.geppetto.pp.pptp.PPTPPackage.eNS_URI))
			registry.put(com.puppetlabs.geppetto.pp.pptp.PPTPPackage.eNS_URI, com.puppetlabs.geppetto.pp.pptp.PPTPPackage.eINSTANCE);

		Map<String, Object> factoryMap = Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
		if(!factoryMap.containsKey("pptp"))
			factoryMap.put("pptp", new XMIResourceFactoryImpl());

		EValidator.Registry.INSTANCE.remove(PPFactory.eINSTANCE.getPPPackage());

		Injector mainInjector = super.createInjectorAndDoEMFRegistration();
		doPptpSetup();

		return mainInjector;
	}

	public void doPptpSetup() {
		pptpInjector = Guice.createInjector(getPptpModule());

		// register pptp
		// Expect the pptp resource factory (a default XMI resource) to be available by default
		// register the resource service provider (in a UI scenario this is registered by the Activator).
		IResourceServiceProvider pptpServiceProvider = pptpInjector.getInstance(IResourceServiceProvider.class);
		IResourceServiceProvider.Registry.INSTANCE.getExtensionToFactoryMap().put("pptp", pptpServiceProvider);
	}

	public Injector getPptpInjector() {
		return pptpInjector;
	}

	protected Module getPptpModule() {
		return new PptpRuntimeModule();
	}
}
