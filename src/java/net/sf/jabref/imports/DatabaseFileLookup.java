package net.sf.jabref.imports;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.GUIGlobals;
import net.sf.jabref.JabRef;
import net.sf.jabref.gui.FileListEntry;
import net.sf.jabref.gui.FileListTableModel;

/**
 * Search class for files. <br>
 * <br>
 * This class provides some functionality to search in a {@link BibtexDatabase} for
 * files. <br>
 * <br>
 * 
 * 
 * @author Nosh&Dan
 * @version 09.11.2008 | 21:21:41
 * 
 */
public class DatabaseFileLookup {

	private static final String KEY_FILE_FIELD = "file";

	private static Logger logger = Logger.getLogger(DatabaseFileLookup.class.getName());
	
	private final BibtexDatabase database;

	/**
	 * Creates an instance by passing a {@link BibtexDatabase} which will be
	 * used for the searches.
	 * 
	 * @param aDatabase
	 *            A {@link BibtexDatabase}.
	 */
	public DatabaseFileLookup(BibtexDatabase aDatabase) {
		if (aDatabase == null)
			throw new IllegalArgumentException("Passing a 'null' BibtexDatabase.");
		this.database = aDatabase;
	}
	
	/**
	 * Returns whether the File <code>aFile</code> is present in the database
	 * as an attached File to an {@link BibtexEntry}. <br>
	 * <br>
	 * To do this, the field specified by the key <b>file</b> will be searched
	 * for the provided file for every {@link BibtexEntry} in the database. <br>
	 * <br>
	 * For the matching, the absolute file paths will be used.
	 * 
	 * @param file
	 *            A {@link File} Object.
	 * @return <code>true</code>, if the file Object is stored in at least one
	 *         entry in the database, otherwise <code>false</code>.
	 */
	public boolean lookupDatabase(File aFile) {
		Collection<BibtexEntry> entries = database.getEntries();
		for (BibtexEntry entry : entries) {
			if (lookupEntry(aFile, entry)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Searches the specified {@link BibtexEntry} <code>anEntry</code> for the
	 * appearance of the specified {@link File} <code>aFile</code>. <br>
	 * <br>
	 * Therefore the <i>file</i>-field of the bibtex-entry will be searched for
	 * the absolute filepath of the searched file. <br>
	 * <br>
	 * 
	 * @param aFile
	 *            A file that is searched in an bibtex-entry.
	 * @param anEntry
	 *            A bibtex-entry, in which the file is searched.
	 * @return <code>true</code>, if the bibtex entry stores the file in its
	 *         <i>file</i>-field, otherwise <code>false</code>.
	 */
	public boolean lookupEntry(File aFile, BibtexEntry anEntry) {

		if (aFile == null || anEntry == null)
			return false;

		FileListTableModel model = new FileListTableModel();

		String fileField = anEntry.getField(KEY_FILE_FIELD);
		model.setContent(fileField);
		
		File asFile = null;
		for (int i = 0; i < model.getRowCount(); i++) {
			FileListEntry flEntry = model.getEntry(i);
			String link = flEntry.getLink();
			
			if (link == null) {
				break;
			}
			
			String[] possibleFilePaths = JabRef.jrf.basePanel().metaData().getFileDirectory(GUIGlobals.FILE_FIELD);
			try {
				asFile = new File(link);
				boolean exists = false;
				
				if(asFile.exists()) {
					exists = true;
				}else {
					// bibtexfilepath is relative path
					// so try to get full path from possibleFilePaths
					exists = false;
					for(String s : possibleFilePaths) {
						//System.out.println("trying: " + s+File.separatorChar+link);
						asFile = new File(s+File.separatorChar+link);
						if(asFile.exists()) {
							// leave loop if file exists
							exists = true;
							break;
						}	
					}
				}
				// file was found creating some possible absolute path for it
				if (exists && FileUtils.contentEquals(aFile, asFile)) {
					return true;
				}
				
			} catch (IOException e) {
				logger.info("IOException");
			} 
		}
		
		return false;
	}
}