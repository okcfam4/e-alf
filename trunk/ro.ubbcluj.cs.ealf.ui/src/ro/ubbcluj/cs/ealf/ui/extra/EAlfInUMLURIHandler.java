package ro.ubbcluj.cs.ealf.ui.extra;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.impl.PlatformResourceURIHandlerImpl;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.uml2.common.edit.command.ChangeCommand;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.editor.presentation.UMLEditor;
import org.eclipse.uml2.uml.resource.UMLResource;

public class EAlfInUMLURIHandler extends PlatformResourceURIHandlerImpl {

	private UMLEditor umlEditor;
	private UMLResource umlResource;

	public EAlfInUMLURIHandler(UMLEditor umlEditor, UMLResource umlResource) {
		this.umlResource = umlResource;
		this.umlEditor = umlEditor;
	}

	private Comment findComment(EList<EObject> contents, String commentID) {
		if (contents != null) {
			for (int i = 0; i < contents.size(); i++) {
				EObject o = contents.get(i);
				// System.out.println(umlResource.getID(o) + " " + o);
				if (o instanceof Comment && umlResource.getID(o) != null
						&& umlResource.getID(o).equals(commentID)) {
					return (Comment) o;
				}
				// Comment comment = findComment(o.eContents(), commentID);
				// if (comment != null) {
				// return comment;
				// }
			}
		}
		return null;
	}

	private Comment getComment(URI uri) {
		Comment comment = null;
		// comment = (Comment) umlResource.getEObject(uri.query());
		EList<EObject> contents = umlResource.getContents().get(0).eContents();
		comment = findComment(contents, uri.query());
		if (comment == null) {
			System.out.println("Couldn't find comment for " + uri);
		}
		// String id = umlResource.getID(o);
		return comment;
	}

	@Override
	public boolean canHandle(URI uri) {
		if (uri.scheme().equals("ealf") && uri.fileExtension().equals("uml")
				&& uri.query() != null) {
			// System.out.println("EAlfURIHandler.canHandle " + uri);
			return true;
		}
		return false;
	}

	@Override
	public InputStream createInputStream(URI uri, Map<?, ?> options)
			throws IOException {
		Comment comment = getComment(uri);
		return new ByteArrayInputStream(comment.getBody().getBytes());
	}

	@Override
	public OutputStream createOutputStream(final URI uri,
			final Map<?, ?> options) throws IOException {
		return new ByteArrayOutputStream() {
			@Override
			public void close() throws IOException {
				super.close();
				final String content = toString();

				umlEditor.getEditorSite().getShell().getDisplay()
						.asyncExec(new Runnable() {
							@Override
							public void run() {
								EditingDomain editingDomain = umlEditor
										.getEditingDomain();
								editingDomain.getCommandStack().execute(
										new ChangeCommand(
												editingDomain,
												new Runnable() {
													public void run() {
														getComment(uri)
																.setBody(
																		content);
													}
												},
												"Saved Activity text in comment"));
								umlEditor.doSave(null);
							}
						});

				// umlResource.save(null);
			}
		};
	}

	@Override
	public void delete(URI uri, Map<?, ?> options) throws IOException {
		System.out.println("EAlfURIHandler.delete");
	}

	@Override
	public boolean exists(URI uri, Map<?, ?> options) {
		boolean exists = super.exists(EAlfUtil.getMainUMLURI(uri), options);
		return exists;
	}

	@Override
	public Map<String, ?> getAttributes(URI uri, Map<?, ?> options) {
		return super.getAttributes(EAlfUtil.getMainUMLURI(uri), options);
	}

	@Override
	public void setAttributes(URI uri, Map<String, ?> attributes,
			Map<?, ?> options) throws IOException {
		super.setAttributes(EAlfUtil.getMainUMLURI(uri), attributes, options);
	}

}
