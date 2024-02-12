package services

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.auth.oauth2.TokenResponseException
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.*
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import kotlin.system.exitProcess


class SheetsService {

    private val sheets: Sheets
    private val spreadsheetId = "1oOMtTfWdAGHR-75xZjxBW16ndXPEhJ0jH3sFvL-Tz24"
    private val APPLICATION_NAME = "Google Sheets API Java Quickstart"
    private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
    private val TOKENS_DIRECTORY_PATH = "tokens"

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private val SCOPES = listOf(SheetsScopes.SPREADSHEETS)
    private val CREDENTIALS_FILE_PATH = "/credentials.json"

    // Header details
    private val headerList = listOf(listOf("number", "name", "production", "variant", "condition", "low", "high", "quantity"))
    private val headerRange = "A1:H1"

    init {
        val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        sheets = Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build()
    }

    fun getValues(range: String): MutableList<MutableList<Any>>? {
        return try {
            val response = sheets.spreadsheets().values()[spreadsheetId, range]
                .execute()
            response.getValues()
        } catch (e: TokenResponseException) {
            val deleted = File(TOKENS_DIRECTORY_PATH).delete()
            if (deleted) {
                // TODO: implement retry limit
                getValues(range)
            } else {
                throw e
            }
        }
    }

    fun insertValues(range: String, values: ValueRange) {
        println("Inserting values: $values")
        val request = sheets.spreadsheets().values().update(spreadsheetId, range, values).setValueInputOption("RAW")

        val response = request.execute()
        println("Response: $response")
    }

    // range is expected to be in the format <name>!<cell range>
    fun createSheet(range: String) {
        val name = range.split("!")[0]
        val requests: MutableList<Request> = ArrayList()
        requests.add(
            Request().setAddSheet(
                AddSheetRequest().setProperties(
                    SheetProperties()
                        .setTitle(name)
                )
            )
        )
        val body = BatchUpdateSpreadsheetRequest().setRequests(requests)

        sheets.spreadsheets().batchUpdate(spreadsheetId, body).execute()
        val values = ValueRange()
        values.setValues(headerList)
        insertValues("$name!$headerRange", values)
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    @Throws(IOException::class)
    private fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential {
        // Load client secrets.
        val `in` = SheetsService::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH)
            ?: throw FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH)
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(`in`))

        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES
        )
            .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build()
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }
}