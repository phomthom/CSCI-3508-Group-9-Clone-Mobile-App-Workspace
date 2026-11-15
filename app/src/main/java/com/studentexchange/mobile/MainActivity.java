package com.studentexchange.mobile;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.*;
import android.text.method.PasswordTransformationMethod;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;
import com.studentexchange.mobile.api.ApiClient;
import com.studentexchange.mobile.api.MarketplaceApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Native StudentExchange UI with a read-only, emulator-local mock marketplace. */
public class MainActivity extends Activity {
 final int GREEN=Color.rgb(42,141,119), INK=Color.rgb(25,46,42), MUTED=Color.rgb(105,120,114), BG=Color.rgb(246,249,247), LINE=Color.rgb(221,230,224);
 String[] categories={"All finds"};
 final ArrayList<String> conditions=new ArrayList<>();
 String conditionFilter="All conditions";
 MarketplaceApi api;
 final android.os.Handler networkHandler=new android.os.Handler(android.os.Looper.getMainLooper());
 final ArrayList<Call<?>> activeCalls=new ArrayList<>();
 Call<List<MarketplaceApi.Listing>> listingCall;
 Runnable pendingSearch;
 boolean loadingListings=false,loadingDirectories=false,directoriesReady=false;
 String listingError="",directoryError="";
 int listingGeneration=0;
 LinearLayout categoryChips;
 android.content.SharedPreferences prefs;
 LinearLayout root,body,grid; String activeItem=""; String tab="Explore",category="All finds",query="",session="",photo="",screen=""; boolean usedOnly=false; double maxPrice=10000;
 ArrayList<Item> items=new ArrayList<>(); Set<String> saved=new HashSet<>(); JSONArray messages=new JSONArray();
 static class Item {String id,title,category,description,campus,image,owner; String condition="",seller=""; double price; boolean used=true;
 JSONObject json() throws JSONException {JSONObject j=new JSONObject(); j.put("id",id).put("title",title).put("category",category).put("description",description).put("campus",campus).put("image",image).put("owner",owner).put("price",price).put("used",used); return j;}
 Item(JSONObject j){id=j.optString("id");title=j.optString("title");category=j.optString("category");description=j.optString("description");campus=j.optString("campus");image=j.optString("image");owner=j.optString("owner");price=j.optDouble("price");used=j.optBoolean("used",true);}
 }
 @Override public void onCreate(Bundle state){super.onCreate(state);api=ApiClient.create();prefs=getSharedPreferences("studentexchange",MODE_PRIVATE);session=prefs.getString("session","");saved=new HashSet<>(prefs.getStringSet("saved",new HashSet<>()));load();if(state!=null){
 tab=state.getString("tab","Explore");category=state.getString("category","All finds");query=state.getString("query","");photo=state.getString("photo","");usedOnly=state.getBoolean("usedOnly");maxPrice=state.getDouble("maxPrice",10000);activeItem=state.getString("item","");conditionFilter=state.getString("conditionFilter","All conditions");
 String page=state.getString("screen","home");
 if(page.equals("onboarding"))onboarding();else if(page.equals("login")||page.equals("register"))auth(page.equals("register"));else if(page.equals("detail")){Item match=null;for(Item i:items)if(i.id.equals(activeItem))match=i;if(match!=null)detail(match);else if(activeItem.matches("[1-9][0-9]*")){try{detail(new Item(new JSONObject().put("id",activeItem)));}catch(JSONException ignored){home(tab);}}else home(tab);}else home(tab);
 ArrayList<String> values=state.getStringArrayList("fields");if(values!=null){ArrayList<EditText> fields=new ArrayList<>();collectFields(root,fields);for(int i=0;i<Math.min(fields.size(),values.size());i++)fields.get(i).setText(values.get(i));}
 }else if(prefs.getBoolean("onboarded",false))home("Explore");else onboarding();}
 int d(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 GradientDrawable shape(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(d(radius));g.setStroke(d(1),color==Color.WHITE?LINE:color);return g;}
 LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
 LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
 void pad(View v,int n){v.setPadding(d(n),d(n),d(n),d(n));}
 void gap(LinearLayout l,int n){View v=new View(this);l.addView(v,new LinearLayout.LayoutParams(1,d(n)));}
 TextView text(String s,int size,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(INK);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
 void label(LinearLayout l,String s,int size,boolean bold){l.addView(text(s,size,bold));}
 void copy(LinearLayout l,String s){TextView t=text(s,16,false);t.setTextColor(MUTED);t.setLineSpacing(d(5),1);l.addView(t);}
 Button button(String s,boolean primary,Runnable r){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextSize(16);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setTextColor(primary?Color.WHITE:GREEN);b.setBackground(shape(primary?GREEN:Color.WHITE,16));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,d(54));p.topMargin=d(12);b.setLayoutParams(p);b.setOnClickListener(v->r.run());return b;}
 void action(LinearLayout l,String s,boolean primary,Runnable r){l.addView(button(s,primary,r));}
 EditText field(LinearLayout l,String title,String hint,boolean password){gap(l,18);label(l,title,14,true);gap(l,8);EditText e=new EditText(this);e.setSingleLine(true);e.setTextSize(16);e.setTextColor(INK);e.setHint(hint);e.setHintTextColor(MUTED);pad(e,14);e.setBackground(shape(Color.WHITE,14));e.setInputType(password?129:1);l.addView(e,new LinearLayout.LayoutParams(-1,d(54)));return e;}
 void base(String name){listingGeneration++;if(listingCall!=null)listingCall.cancel();if(pendingSearch!=null)networkHandler.removeCallbacks(pendingSearch);screen=name;root=col();root.setBackgroundColor(BG);setContentView(root);}
 LinearLayout scroll(){ScrollView s=new ScrollView(this);s.setFillViewport(true);body=col();pad(body,22);s.addView(body);root.addView(s,new LinearLayout.LayoutParams(-1,0,1));return body;}
 void brand(LinearLayout l){ImageView v=new ImageView(this);v.setImageResource(R.drawable.brand);v.setScaleType(ImageView.ScaleType.FIT_START);v.setContentDescription("StudentExchange");l.addView(v,new LinearLayout.LayoutParams(-1,d(43)));}
 void image(LinearLayout l,String source,int height){ImageView v=new ImageView(this);setImage(v,source);v.setScaleType(ImageView.ScaleType.FIT_CENTER);v.setBackground(shape(Color.rgb(237,233,243),18));v.setClipToOutline(true);l.addView(v,new LinearLayout.LayoutParams(-1,d(height)));}
 void setImage(ImageView v,String source){if(source.equals("headphones"))v.setImageResource(R.drawable.headphones);else if(source.equals("calculus"))v.setImageResource(R.drawable.calculus);else if(source.equals("books"))v.setImageResource(R.drawable.onboarding_books);else {try{
 android.graphics.BitmapFactory.Options o=new android.graphics.BitmapFactory.Options();o.inJustDecodeBounds=true;
 try(java.io.InputStream in=getContentResolver().openInputStream(Uri.parse(source))){android.graphics.BitmapFactory.decodeStream(in,null,o);}
 o.inSampleSize=1;while(o.outWidth/o.inSampleSize>1200||o.outHeight/o.inSampleSize>1200)o.inSampleSize*=2;o.inJustDecodeBounds=false;
 try(java.io.InputStream in=getContentResolver().openInputStream(Uri.parse(source))){v.setImageBitmap(android.graphics.BitmapFactory.decodeStream(in,null,o));}
 if(v.getDrawable()==null)v.setImageResource(R.drawable.onboarding_books);
 }catch(Exception e){v.setImageResource(R.drawable.onboarding_books);}}}
 void onboarding(){base("onboarding");LinearLayout l=scroll();brand(l);action(l,"Skip →",false,()->{prefs.edit().putBoolean("onboarded",true).apply();home("Explore");});gap(l,20);image(l,"books",265);gap(l,26);TextView over=text("MADE FOR YOUR NEXT CHAPTER",13,true);over.setTextColor(GREEN);l.addView(over);gap(l,10);label(l,"Your campus.\nYour exchange.",38,true);gap(l,18);copy(l,"A little less spending. A little more student life. Find a new home for the things you love.");gap(l,20);action(l,"Continue →",true,()->{prefs.edit().putBoolean("onboarded",true).apply();auth(true);});action(l,"Already here? Log in",false,()->auth(false));gap(l,12);copy(l,"Development preview · Local mock marketplace");}
 void auth(boolean register){base(register?"register":"login");LinearLayout l=scroll();action(l,"← Back to marketplace",false,()->home("Explore"));gap(l,25);brand(l);gap(l,30);TextView o=text("YOUR CAMPUS CONNECTION",13,true);o.setTextColor(GREEN);l.addView(o);gap(l,12);label(l,register?"Your next chapter.":"Welcome back.",36,true);gap(l,16);copy(l,register?"Create your campus account and find your next great thing.":"Your next great find is just around the corner.");EditText name=register?field(l,"Full name","Your name",false):null;EditText email=field(l,"Email address","you@university.edu",false);email.setInputType(33);EditText password=field(l,"Password",register?"At least 8 characters":"Enter your password",true);action(l,"Show / hide password",false,()->{password.setTransformationMethod(password.getTransformationMethod()==null?PasswordTransformationMethod.getInstance():null);password.setSelection(password.length());});
 action(l,register?"Create account →":"Log in →",true,()->{
 String mail=email.getText().toString().trim().toLowerCase(Locale.ROOT),pw=password.getText().toString();
 if(!android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()){email.setError("Enter a valid email address");return;}
 if(pw.length()<8){password.setError("Use at least 8 characters");return;}
 if(register){if(mail.equals("demo@university.edu")){email.setError("This address is reserved for the demo account");return;}if(name.length()==0){name.setError("Enter your name");return;}if(prefs.contains("account."+mail)){email.setError("Account already exists on this device");return;}String salt=UUID.randomUUID().toString();prefs.edit().putString("account."+mail,salt+":"+hash(pw,salt)).putString("name."+mail,name.getText().toString().trim()).apply();}
 else if(!(mail.equals("demo@university.edu")&&pw.equals("CampusDemo123"))){String stored=prefs.getString("account."+mail,"");String[] parts=stored.split(":");if(parts.length!=2||!hash(pw,parts[0]).equals(parts[1])){password.setError("Email or password does not match a local account");return;}}
 session=mail;prefs.edit().putString("session",session).apply();home("Explore");});
 if(!register){action(l,"Forgot password?",false,()->info("Account recovery","This offline prototype cannot send reset emails. Use the demo account or register another local account."));gap(l,16);LinearLayout demo=col();pad(demo,16);demo.setBackground(shape(BG,18));label(demo,"Take it for a test drive.",18,true);copy(demo,"No real credentials needed. This prototype uses a public sample account.");action(demo,"Fill demo account",false,()->{email.setText("demo@university.edu");password.setText("CampusDemo123");});l.addView(demo);}
 action(l,register?"Already here? Log in":"New here? Create account",false,()->auth(!register));action(l,"Explore without logging in →",false,()->home("Explore"));gap(l,14);copy(l,"Accounts are stored only on this device. Use a sample password.");}
 String hash(String pw,String salt){try{return android.util.Base64.encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(new PBEKeySpec(pw.toCharArray(),salt.getBytes(java.nio.charset.StandardCharsets.UTF_8),120000,256)).getEncoded(),2);}catch(Exception e){throw new IllegalStateException(e);}}
 class NavIcon extends View {
 final int index,tint; NavIcon(int index,int tint){super(MainActivity.this);this.index=index;this.tint=tint;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
 @Override protected void onDraw(android.graphics.Canvas c){super.onDraw(c);c.save();c.scale(getWidth()/24f,getHeight()/24f);android.graphics.Paint p=new android.graphics.Paint(3);p.setColor(tint);p.setStyle(android.graphics.Paint.Style.STROKE);p.setStrokeWidth(1.7f);p.setStrokeCap(android.graphics.Paint.Cap.ROUND);p.setStrokeJoin(android.graphics.Paint.Join.ROUND);android.graphics.Path path=new android.graphics.Path();
 if(index==0){path.moveTo(3,10);path.lineTo(12,3);path.lineTo(21,10);path.lineTo(21,21);path.lineTo(15,21);path.lineTo(15,14);path.lineTo(9,14);path.lineTo(9,21);path.lineTo(3,21);path.close();c.drawPath(path,p);}
 else if(index==1){path.moveTo(12,21);path.cubicTo(9,18,2,13,2,8);path.cubicTo(2,2,9,1,12,6);path.cubicTo(15,1,22,2,22,8);path.cubicTo(22,13,15,18,12,21);c.drawPath(path,p);}
 else if(index==2){c.drawLine(12,4,12,20,p);c.drawLine(4,12,20,12,p);}
 else if(index==3){path.moveTo(4,20);path.lineTo(5,15);path.cubicTo(-2,3,20,-1,21,9);path.cubicTo(23,17,12,21,7,18);path.close();c.drawPath(path,p);}
 else{c.drawCircle(12,7,4,p);path.moveTo(4,22);path.cubicTo(3,10,21,10,20,22);c.drawPath(path,p);}c.restore();}
 }
 void header(){LinearLayout h=row();pad(h,16);ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.brand);logo.setScaleType(ImageView.ScaleType.FIT_START);logo.setContentDescription("StudentExchange");h.addView(logo,new LinearLayout.LayoutParams(0,d(36),1));TextView info=text(" ⓘ ",24,false);info.setContentDescription("About StudentExchange");info.setOnClickListener(v->info("StudentExchange","Good finds. Better together.\n\nLocal mock marketplace. Listings come from the development adapter. Saved items and demo accounts stay on this device."));h.addView(info);root.addView(h);}
 void home(String selected){tab=selected;base("home");header();LinearLayout l=scroll();if(selected.equals("Explore")||selected.equals("Saved"))market(l,selected.equals("Saved"));else if(selected.equals("Post"))post(l);else if(selected.equals("Inbox"))inbox(l);else account(l);LinearLayout nav=row();nav.setBackgroundColor(Color.WHITE);String[] icons={"⌂","♡","＋","☏","○"};String[] names={"Explore","Saved","Post","Inbox","Account"};for(int i=0;i<names.length;i++){String n=names[i];LinearLayout b=col();b.setGravity(Gravity.CENTER);int tint=n.equals(tab)?GREEN:MUTED;NavIcon icon=new NavIcon(i,tint);b.addView(icon,new LinearLayout.LayoutParams(d(27),d(27)));TextView caption=text(n,11,n.equals(tab));caption.setTextColor(tint);b.addView(caption);b.setContentDescription(n);b.setOnClickListener(v->home(n));nav.addView(b,new LinearLayout.LayoutParams(0,d(64),1));}root.addView(nav);}
 void market(LinearLayout l,boolean onlySaved){if(!onlySaved){LinearLayout hero=row();pad(hero,18);hero.setBackground(shape(Color.rgb(232,243,236),22));LinearLayout words=col();label(words,"Good finds.\nBetter together.",27,true);gap(words,12);copy(words,"Buy less new.\nMake room for more.");hero.addView(words,new LinearLayout.LayoutParams(0,-2,1));ImageView art=new ImageView(this);art.setImageResource(R.drawable.onboarding_books);art.setScaleType(ImageView.ScaleType.FIT_CENTER);hero.addView(art,new LinearLayout.LayoutParams(d(95),d(150)));l.addView(hero);gap(l,16);}
 EditText search=field(l,"Search campus finds","What are you looking for?",false);search.setText(query);action(l,"Filters",false,()->filters());HorizontalScrollView hs=new HorizontalScrollView(this);hs.setHorizontalScrollBarEnabled(false);LinearLayout chips=row();categoryChips=chips;renderCategories();hs.addView(chips);l.addView(hs);label(l,onlySaved?"Saved finds":"Fresh on campus",24,true);gap(l,16);grid=col();l.addView(grid);renderGrid(onlySaved);search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){query=s.toString();if(pendingSearch!=null)networkHandler.removeCallbacks(pendingSearch);listingGeneration++;if(listingCall!=null)listingCall.cancel();loadingListings=true;renderGrid(onlySaved);pendingSearch=()->fetchListings();networkHandler.postDelayed(pendingSearch,350);}public void afterTextChanged(Editable e){}});fetchListings();loadDirectories();}
 void renderGrid(boolean onlySaved){grid.removeAllViews();
 if(loadingListings){grid.addView(new ProgressBar(this));copy(grid,"Loading campus finds…");return;}
 if(!listingError.isEmpty()){copy(grid,listingError);action(grid,"Retry",false,()->{fetchListings();loadDirectories();});return;}
 if(!directoryError.isEmpty()){copy(grid,directoryError);action(grid,"Retry filters",false,()->loadDirectories());}
ArrayList<Item> filtered=new ArrayList<>();for(Item i:items)if((!onlySaved||saved.contains(i.id))&&(category.equals("All finds")||category.equals(i.category))&&(!usedOnly||i.used)&&i.price<=maxPrice&&(conditionFilter.equals("All conditions")||conditionFilter.equals(i.condition)))filtered.add(i);if(filtered.isEmpty()){copy(grid,"No finds yet. Try another search or save a listing with the heart button.");return;}copy(grid,filtered.size()+" finds");gap(grid,8);for(int k=0;k<filtered.size();k+=2){LinearLayout r=row();r.setGravity(Gravity.TOP);for(int n=k;n<Math.min(k+2,filtered.size());n++){Item i=filtered.get(n);LinearLayout card=col();card.setBackground(shape(Color.WHITE,18));card.setClipToOutline(true);ImageView img=new ImageView(this);setImage(img,i.image);img.setContentDescription(i.title);img.setScaleType(ImageView.ScaleType.CENTER_CROP);FrameLayout frame=new FrameLayout(this);frame.addView(img,new FrameLayout.LayoutParams(-1,-1));if(i.id.startsWith("sample-")){TextView badge=text("SAMPLE",10,true);badge.setTextColor(MUTED);pad(badge,6);badge.setBackground(shape(Color.WHITE,10));FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(-2,-2,Gravity.TOP|Gravity.START);bp.setMargins(d(8),d(8),0,0);frame.addView(badge,bp);}card.addView(frame,new LinearLayout.LayoutParams(-1,d(152)));img.setOnClickListener(v->detail(i));LinearLayout content=col();pad(content,10);label(content,money(i.price),23,true);TextView title=text(i.title,15,true);content.addView(title);title.setOnClickListener(v->detail(i));gap(content,6);copy(content,i.campus);Button heart=button(saved.contains(i.id)?"♥ Saved":"♡ Save",false,()->{toggle(i);renderGrid(onlySaved);});heart.setContentDescription("Save "+i.title);content.addView(heart);card.addView(content);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.setMargins(0,0,d(8),d(14));r.addView(card,p);}if(k+1==filtered.size())r.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));grid.addView(r);}}
 String money(double price){return String.format(Locale.US,price==Math.floor(price)?"$%.0f":"$%.2f",price);}
 void toggle(Item i){if(!saved.add(i.id))saved.remove(i.id);prefs.edit().putStringSet("saved",new HashSet<>(saved)).apply();}
 void filters(){LinearLayout l=col();pad(l,20);EditText price=field(l,"Maximum price ($)","Any price",false);price.setInputType(8194);if(maxPrice<10000)price.setText(String.valueOf(maxPrice));gap(l,16);label(l,"Condition",14,true);Spinner condition=new Spinner(this);ArrayList<String> options=new ArrayList<>();options.add("All conditions");options.addAll(conditions);condition.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,options));condition.setSelection(Math.max(0,options.indexOf(conditionFilter)));l.addView(condition);if(!directoriesReady)copy(l,"Condition choices are unavailable until filters load. Retry from the marketplace.");CheckBox used=new CheckBox(this);used.setText("Used items only");used.setChecked(usedOnly);l.addView(used);AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Find your next great thing").setView(l).setPositiveButton("Apply",null).setNeutralButton("Reset",(a,b)->{maxPrice=10000;usedOnly=false;conditionFilter="All conditions";category="All finds";query="";home(tab);}).setNegativeButton("Cancel",null).create();dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(w->{try{double p=price.length()==0?10000:Double.parseDouble(price.getText().toString());if(!Double.isFinite(p)||p<0)throw new NumberFormatException();maxPrice=p;usedOnly=used.isChecked();conditionFilter=options.get(condition.getSelectedItemPosition());dialog.dismiss();home(tab);}catch(Exception e){price.setError("Enter a valid positive price");}}));dialog.show();}
 void showDetail(Item i){activeItem=i.id;base("detail");LinearLayout l=scroll();action(l,"← Campus finds",false,()->home(tab));gap(l,16);label(l,"Campus find",24,true);gap(l,18);image(l,i.image,310);gap(l,20);label(l,money(i.price),34,true);gap(l,8);label(l,i.title,26,true);gap(l,14);copy(l,i.condition+"  ·  "+i.category);gap(l,10);copy(l,"Seller: "+(i.seller.isEmpty()?i.owner:i.seller));gap(l,14);copy(l,"⌖ "+i.campus);gap(l,16);copy(l,i.description);action(l,saved.contains(i.id)?"♥ Saved":"♡ Save find",false,()->{toggle(i);showDetail(i);});action(l,"Message seller",true,()->{info("Messaging unavailable","This preview supports reading mock marketplace listings only.");});}

 boolean requireLogin(){if(!session.isEmpty())return true;new AlertDialog.Builder(this).setTitle("Join your campus").setMessage("Log in or create a local account to post listings and send messages.").setPositiveButton("Log in",(a,b)->auth(false)).setNegativeButton("Cancel",null).show();return false;}
 void post(LinearLayout l){label(l,"Pass it on.",32,true);copy(l,"Posting is unavailable in this read-only development preview.");}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==20&&result==RESULT_OK&&data!=null&&data.getData()!=null){Uri uri=data.getData();try{getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);photo=uri.toString();ImageView preview=root.findViewWithTag("photoPreview");if(preview!=null)setImage(preview,photo);TextView status=root.findViewWithTag("photoStatus");if(status!=null)status.setText("Personal photo selected");Toast.makeText(this,"Photo selected. Your form is still ready to publish.",Toast.LENGTH_LONG).show();}catch(SecurityException e){info("Photo access","Choose a photo that allows persistent access.");}}}
 void compose(Item i){LinearLayout l=col();pad(l,20);EditText message=field(l,"Message","Hi, is this still available?",false);message.setSingleLine(false);AlertDialog dialog=new AlertDialog.Builder(this).setTitle(i.title).setView(l).setPositiveButton("Send locally",null).setNegativeButton("Cancel",null).create();dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(w->{if(message.getText().toString().trim().isEmpty()){message.setError("Write a message first");return;}try{messages.put(new JSONObject().put("account",session).put("item",i.id).put("title",i.title).put("text",message.getText().toString().trim()).put("time",System.currentTimeMillis()));persist();dialog.dismiss();home("Inbox");}catch(JSONException e){info("Unable to save","Try again.");}}));dialog.show();}
 void inbox(LinearLayout l){label(l,"Your campus conversations",28,true);gap(l,12);copy(l,"Local demo inbox. Messages are saved on this device and are not delivered to other people.");if(session.isEmpty()){action(l,"Log in to view messages",true,()->auth(false));return;}int count=0;for(int n=messages.length()-1;n>=0;n--){JSONObject m=messages.optJSONObject(n);if(m==null||!session.equals(m.optString("account")))continue;count++;gap(l,16);LinearLayout card=col();pad(card,16);card.setBackground(shape(Color.WHITE,18));label(card,m.optString("title"),18,true);gap(card,6);copy(card,m.optString("text"));gap(card,6);copy(card,"You · "+android.text.format.DateFormat.format("MMM d, h:mm a",m.optLong("time")));for(Item i:items)if(i.id.equals(m.optString("item")))action(card,"Open listing / send another message",false,()->detail(i));l.addView(card);}if(count==0){gap(l,26);copy(l,"No conversations yet. Open a find and tap Message seller to begin.");}}
 void account(LinearLayout l){label(l,"Your corner of campus",28,true);gap(l,14);if(session.isEmpty()){copy(l,"Join the exchange to pass things on and connect with your campus.");action(l,"Log in",true,()->auth(false));action(l,"Create account",false,()->auth(true));return;}label(l,prefs.getString("name."+session,session.equals("demo@university.edu")?"Alex · Demo student":"Campus student"),22,true);copy(l,session);gap(l,20);copy(l,"Saved finds: "+saved.size());action(l,"View saved finds",false,()->home("Saved"));action(l,"Post a campus find",true,()->home("Post"));gap(l,24);label(l,"My listings",22,true);int count=0;for(Item i:items)if(session.equals(i.owner)){count++;action(l,i.title+" · "+money(i.price),false,()->detail(i));}if(count==0)copy(l,"Your next chapter starts with your first listing.");action(l,"Log out",false,()->{session="";prefs.edit().remove("session").apply();home("Account");});action(l,"Replay welcome screen",false,()->onboarding());}
 void info(String title,String content){new AlertDialog.Builder(this).setTitle(title).setMessage(content).setPositiveButton("OK",null).show();}
 void load(){try{messages=new JSONArray(prefs.getString("messages","[]"));}catch(JSONException ignored){messages=new JSONArray();}}
 void persist(){JSONArray a=new JSONArray();try{for(Item i:items)if(!i.id.matches("[1-9][0-9]*"))a.put(i.json());prefs.edit().putString("items",a.toString()).putString("messages",messages.toString()).apply();}catch(JSONException e){info("Unable to save","Local data could not be saved.");}}

 boolean marketplaceVisible(){return screen.equals("home")&&(tab.equals("Explore")||tab.equals("Saved"));}
 String networkError(){return "Could not reach the local marketplace. Start the development adapter on your Mac, then retry.";}
 <T> void request(Call<T> call, java.util.function.Consumer<T> success, Runnable failure){
  activeCalls.add(call);
  call.enqueue(new Callback<T>(){
   public void onResponse(Call<T> c,Response<T> response){activeCalls.remove(c);if(isFinishing()||isDestroyed()||c.isCanceled())return;
    if(response.isSuccessful()&&response.body()!=null){try{success.accept(response.body());}catch(RuntimeException e){failure.run();}}
    else failure.run();
    if(response.errorBody()!=null)response.errorBody().close();
   }
   public void onFailure(Call<T> c,Throwable error){activeCalls.remove(c);if(!c.isCanceled()&&!isFinishing()&&!isDestroyed())failure.run();}
  });
 }
 void renderCategories(){
  if(categoryChips==null)return;categoryChips.removeAllViews();
  for(String c:categories){TextView b=text(c,13,false);pad(b,12);b.setTextColor(category.equals(c)?Color.WHITE:INK);b.setBackground(shape(category.equals(c)?GREEN:Color.WHITE,30));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,-2);p.setMargins(0,d(14),d(8),d(14));categoryChips.addView(b,p);b.setOnClickListener(v->{category=c;renderCategories();fetchListings();});}
 }
 void loadDirectories(){
  if(directoriesReady||loadingDirectories)return;
  loadingDirectories=true;directoryError="";final int[] left={2};final boolean[] failed={false};
  Runnable done=()->{if(--left[0]!=0)return;loadingDirectories=false;directoriesReady=!failed[0];if(failed[0])directoryError="Some marketplace filters could not load.";if(marketplaceVisible()){renderCategories();renderGrid(tab.equals("Saved"));}};
  request(api.categories(),names->{ArrayList<String> values=new ArrayList<>();values.add("All finds");for(String name:names)if(name!=null&&!name.trim().isEmpty()&&!values.contains(name))values.add(name);categories=values.toArray(new String[0]);done.run();},()->{failed[0]=true;done.run();});
  request(api.conditions(),names->{conditions.clear();for(String name:names)if(name!=null&&!name.trim().isEmpty()&&!conditions.contains(name))conditions.add(name);done.run();},()->{failed[0]=true;done.run();});
 }
 Item apiItem(MarketplaceApi.Listing value,String id){
  if(value.title==null||value.category==null||value.condition==null||value.price==null||!Double.isFinite(value.price)||value.price<0)throw new IllegalArgumentException();
  try{
   Item i=new Item(new JSONObject().put("id",id).put("title",value.title).put("category",value.category).put("description",value.description==null?"":value.description).put("campus",value.location==null||value.location.isEmpty()?"Location not provided":value.location).put("image","books").put("owner",value.user==null?"":value.user).put("price",value.price).put("used",!value.condition.equalsIgnoreCase("New")));
   i.condition=value.condition;i.seller=((value.first_name==null?"":value.first_name)+" "+(value.last_name==null?"":value.last_name)).trim();return i;
  }catch(JSONException e){throw new IllegalArgumentException(e);}
 }
 void fetchListings(){
  if(!marketplaceVisible())return;
  if(listingCall!=null)listingCall.cancel();final int generation=++listingGeneration;
  loadingListings=true;listingError="";renderGrid(tab.equals("Saved"));
  listingCall=api.listings(query.trim().isEmpty()?null:query.trim().substring(0,Math.min(200,query.trim().length())),category.equals("All finds")?null:category);
  request(listingCall,values->{if(generation!=listingGeneration||!marketplaceVisible())return;ArrayList<Item> loaded=new ArrayList<>();for(MarketplaceApi.Listing v:values){if(v.id==null||v.id<=0)throw new IllegalArgumentException();loaded.add(apiItem(v,String.valueOf(v.id)));}items=loaded;loadingListings=false;renderGrid(tab.equals("Saved"));},()->{if(generation!=listingGeneration||!marketplaceVisible())return;loadingListings=false;listingError=networkError();renderGrid(tab.equals("Saved"));});
 }
 void detail(Item item){
  if(!item.id.matches("[1-9][0-9]*")){info("Local sample","This item is not part of the mock API marketplace.");return;}
  activeItem=item.id;base("detail");LinearLayout loadingRoot=root;LinearLayout l=scroll();action(l,"← Campus finds",false,()->home(tab));l.addView(new ProgressBar(this));copy(l,"Loading campus find…");
  request(api.detail(item.id),value->{if(root!=loadingRoot)return;showDetail(apiItem(value,item.id));},()->{if(root!=loadingRoot)return;l.removeAllViews();action(l,"← Campus finds",false,()->home(tab));copy(l,"Could not load this mock listing. Check the adapter and try again.");action(l,"Retry",false,()->detail(item));});
 }
 @Override protected void onDestroy(){networkHandler.removeCallbacksAndMessages(null);for(Call<?> call:new ArrayList<>(activeCalls))call.cancel();activeCalls.clear();super.onDestroy();}
 void collectFields(View view,ArrayList<EditText> out){if(view instanceof EditText)out.add((EditText)view);else if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++)collectFields(group.getChildAt(i),out);}}
 @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putString("tab",tab);out.putString("category",category);out.putString("query",query);out.putString("photo",photo);out.putBoolean("usedOnly",usedOnly);out.putDouble("maxPrice",maxPrice);out.putString("item",activeItem);out.putString("screen",screen);out.putString("conditionFilter",conditionFilter);ArrayList<EditText> fields=new ArrayList<>();collectFields(root,fields);ArrayList<String> values=new ArrayList<>();for(EditText f:fields)values.add(f.getInputType()==129?"":f.getText().toString());out.putStringArrayList("fields",values);}
 @Override public void onBackPressed(){if(!screen.equals("home"))home(tab);else if(!tab.equals("Explore"))home("Explore");else super.onBackPressed();}
}
