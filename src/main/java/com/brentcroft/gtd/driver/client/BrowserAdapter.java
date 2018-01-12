package com.brentcroft.gtd.driver.client;

import com.brentcroft.util.Waiter8;
import java.util.Properties;

import static com.brentcroft.util.DateUtils.secondsToMillis;
import static java.lang.String.format;

/**
 * Created by Alaric on 20/03/2017.
 */
public class BrowserAdapter extends DefaultGuiAdapter
{
    protected final String uriInputPath = "descendant-or-self::BorderPane/HBox/TextField[ ( @id='pageAddress' ) ]";
    protected final String uriLoadPath = "descendant-or-self::BorderPane/HBox/Button[ ( @id='pageAddressGet' ) ]";

    protected final String logoutKeys = "{Alt+F4}";


    enum Credential implements Key
    {
        INITIAL_URI( "initial-uri" );

        private final String key;

        Credential( String key )
        {
            this.key = key;
        }

        public String key()
        {
            return key;
        }
    }


    public void setCredentials( Properties newCredentials )
    {
        super.setCredentials( newCredentials );

        for ( Credential c : Credential.values() )
        {
            if ( newCredentials.containsKey( c.key() ) )
            {
                credentials.put( c.key(), newCredentials.get( c.key() ) );
            }
        }
    }

    public void login( double timeoutSeconds )
    {
        String uri = ( String ) credentials.get( Credential.INITIAL_URI.key() );

        if ( uri == null )
        {
            logger.warn( format( "Credentials do not contain [%s]: %s", Credential.INITIAL_URI.key(), credentials ) );

            throw new RuntimeException( format( "Credentials do not contain [%s], NOT assigning uri path.", Credential.INITIAL_URI.key(), uri ) );
        }

        if ( driver == null )
        {
            throw new NullPointerException( "Driver is null!" );
        }


        // is the field available yet
        if ( ! driver.exists( uriInputPath, timeoutSeconds ) )
        {
            throw new RuntimeException( format( "Target not available [%s] after [%s] seconds.", uriInputPath, timeoutSeconds ) );
        }

        driver.setText( uriInputPath, uri, timeoutSeconds );


        new Waiter8()
                .until( () -> uri.equals( driver.getText( uriInputPath ) ) )
                .withTimeoutMillis( secondsToMillis( timeoutSeconds ) )
                .withDelayMillis( 1000 )
                .start();

        logger.info( "Uri path assigned: " + uri );

        driver.click( uriLoadPath, timeoutSeconds );
    }

    public void logout( double timeoutSeconds )
    {
        try
        {
            logger.info( format( "Sending logout keys [%s] to [%s].", logoutKeys, uriInputPath ) );

            driver.robotKeys( uriInputPath, logoutKeys );

            // TODO: can we avoid this?
            Waiter8.delay( 100 );

            logger.info( "Logout completed." );
        }
        catch ( Exception e )
        {
            logger.info( "Logout completed with exception: " + e );
        }
    }
}
